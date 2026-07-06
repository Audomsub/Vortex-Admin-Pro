package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.FileRequest;
import com.vortexadmin.dto.response.FileResponse;
import com.vortexadmin.entity.File;
import com.vortexadmin.entity.User;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.FileRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.FileService;
import com.vortexadmin.util.SecurityUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles file management business logic including metadata registration, multipart
 * file upload to local storage, ownership-scoped download and rename operations,
 * and access-guarded deletion.
 */
@Service
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final java.nio.file.Path fileStorageLocation;

    /**
     * Constructs the service and ensures the {@code uploads} directory exists on the
     * local filesystem. Throws a {@link RuntimeException} if the directory cannot be created.
     *
     * @param fileRepository the repository for persisting file metadata
     * @param userRepository the repository used to look up the uploading user
     */
    public FileServiceImpl(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileStorageLocation = java.nio.file.Paths.get("uploads").toAbsolutePath().normalize();
        try {
            java.nio.file.Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Maps a {@link File} entity to a {@link FileResponse} DTO, using "System" as the
     * uploader username when no user is associated with the file.
     *
     * @param file the file entity to map
     * @return the corresponding file response DTO
     */
    private FileResponse mapFile(File file) {
        return FileResponse.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileUrl(file.getFileUrl())
                .fileType(file.getFileType())
                .size(file.getFileSize())
                .uploadedByUsername(file.getUser() != null ? file.getUser().getUsername() : "System")
                .createdAt(file.getUploadedAt())
                .build();
    }

    /**
     * Returns all file records that belong to the currently authenticated user.
     *
     * @return a list of file response DTOs for the current user's files
     */
    @Override
    public List<FileResponse> getMyFiles() {
        Long userId = SecurityUtils.getCurrentUserId();
        return fileRepository.findByUserId(userId).stream()
                .map(this::mapFile).collect(Collectors.toList());
    }

    /**
     * Persists a file metadata record from an externally supplied URL (e.g. a cloud
     * storage URL already uploaded by the client). Associates the record with the
     * currently authenticated user.
     *
     * @param request the file metadata (file name, URL, type, size)
     * @return the persisted file metadata as a {@link FileResponse} DTO
     * @throws ApiException with {@code 404} if the current user does not exist
     */
    @Override
    @Transactional
    public FileResponse uploadFileRecord(FileRequest request) {
        User uploader = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        File file = File.builder()
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .fileSize(request.getSize())
                .user(uploader)
                .build();
        return mapFile(fileRepository.save(file));
    }

    /**
     * Accepts a multipart file upload, sanitises the filename, writes the file to the
     * local {@code uploads} directory using a UUID prefix to prevent naming collisions,
     * and persists the file metadata associated with the current user.
     * Path traversal attempts (filenames containing "..") are rejected.
     *
     * @param file the multipart file to store
     * @return the persisted file metadata as a {@link FileResponse} DTO
     * @throws ApiException with {@code 400} if the filename contains a path traversal sequence,
     *                      {@code 404} if the current user does not exist, or
     *                      {@code 500} if the file cannot be written to disk
     */
    @Override
    @Transactional
    public FileResponse uploadFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
        try {
            if (fileName.contains("..")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Sorry! Filename contains invalid path sequence " + fileName);
            }
            java.nio.file.Path targetLocation = this.fileStorageLocation.resolve(fileName);
            java.nio.file.Files.copy(file.getInputStream(), targetLocation, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            User uploader = userRepository.findById(SecurityUtils.getCurrentUserId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

            File fileEntity = File.builder()
                    .fileName(originalFileName)
                    .fileUrl(fileName)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .user(uploader)
                    .build();

            return mapFile(fileRepository.save(fileEntity));
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file " + fileName + ". Please try again!");
        }
    }

    /**
     * Retrieves the raw {@link File} entity by id. Used internally by the controller
     * to obtain the entity before delegating to {@link #downloadFile(File)}.
     *
     * @param id the id of the file record to retrieve
     * @return the {@link File} entity
     * @throws ApiException with {@code 404} if no file record with that id exists
     */
    @Override
    public File getFileEntity(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
    }

    /**
     * Resolves the file from local storage as a Spring {@link Resource}.
     * Enforces ownership: administrators with {@code file.read.all} may download any file;
     * other users may only download files they uploaded.
     *
     * @param fileEntity the file metadata entity to serve
     * @return a {@link Resource} pointing to the file on disk
     * @throws ApiException with {@code 403} if the caller does not own the file and
     *                      lacks admin authority, or {@code 404} if the file no longer
     *                      exists on disk
     */
    @Override
    public Resource downloadFile(File fileEntity) {
        boolean isAdmin = currentUserHasAuthority("file.read.all");
        if (!isAdmin && (fileEntity.getUser() == null || !fileEntity.getUser().getId().equals(SecurityUtils.getCurrentUserId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        try {
            java.nio.file.Path filePath = this.fileStorageLocation.resolve(fileEntity.getFileUrl()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ApiException(HttpStatus.NOT_FOUND, "File not found " + fileEntity.getFileName());
            }
        } catch (java.net.MalformedURLException ex) {
            throw new ApiException(HttpStatus.NOT_FOUND, "File not found: " + ex.getMessage());
        }
    }

    /**
     * Checks whether the current Security context principal holds the given authority.
     *
     * @param authority the authority string to check (e.g. "file.read.all")
     * @return {@code true} if the currently authenticated user has the specified authority
     */
    private boolean currentUserHasAuthority(String authority) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    /**
     * Renames a file record's display name. Enforces ownership: administrators with
     * {@code file.read.all} may rename any file; other users may only rename their own.
     *
     * @param id   the id of the file record to rename
     * @param name the new display name for the file
     * @throws ApiException with {@code 404} if the file is not found,
     *                      or {@code 403} if the caller does not own the file and lacks admin authority
     */
    @Override
    @Transactional
    public void renameFile(Long id, String name) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
        boolean isAdmin = currentUserHasAuthority("file.read.all");
        if (!isAdmin && (file.getUser() == null || !file.getUser().getId().equals(SecurityUtils.getCurrentUserId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        file.setFileName(name);
        fileRepository.save(file);
    }

    /**
     * Deletes the file metadata record from the database. Enforces ownership: administrators
     * with {@code file.delete.all} may delete any file record; other users may only delete
     * records they uploaded. Note: the physical file on disk is not removed.
     *
     * @param id the id of the file record to delete
     * @throws ApiException with {@code 404} if the file is not found,
     *                      or {@code 403} if the caller does not own the file and lacks admin authority
     */
    @Override
    @Transactional
    public void deleteFile(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
        boolean isAdmin = currentUserHasAuthority("file.delete.all");
        if (!isAdmin && (file.getUser() == null || !file.getUser().getId().equals(SecurityUtils.getCurrentUserId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        fileRepository.delete(file);
    }
}
