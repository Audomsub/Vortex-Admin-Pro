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

@Service
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final java.nio.file.Path fileStorageLocation;

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

    @Override
    public List<FileResponse> getMyFiles() {
        Long userId = SecurityUtils.getCurrentUserId();
        return fileRepository.findByUserId(userId).stream()
                .map(this::mapFile).collect(Collectors.toList());
    }

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

    @Override
    public File getFileEntity(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
    }

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

    private boolean currentUserHasAuthority(String authority) {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

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
