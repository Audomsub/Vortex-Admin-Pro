package com.vortexadmin.controller;

import com.vortexadmin.dto.request.FileRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.FileResponse;
import com.vortexadmin.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for file management operations, including listing, uploading,
 * downloading, renaming, and deleting files, delegating business logic to FileService.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * Retrieves all files owned by or accessible to the authenticated user.
     *
     * @return a list of {@link FileResponse} objects representing the user's accessible files
     */
    @GetMapping
    @PreAuthorize("hasAuthority('file.read.own') or hasAuthority('file.read.team') or hasAuthority('file.read.all')")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getMyFiles() {
        return ResponseEntity.ok(ApiResponse.success("Files fetched", fileService.getMyFiles()));
    }

    /**
     * Creates a file metadata record without performing a physical file upload (e.g., for externally hosted files).
     *
     * @param request the file metadata payload containing the file name, URL, and type
     * @return the created {@link FileResponse} reflecting the persisted file record
     */
    @PostMapping("/upload-record")
    @PreAuthorize("hasAuthority('file.upload.own')")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFileRecord(@Valid @RequestBody FileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("File record created", fileService.uploadFileRecord(request)));
    }

    /**
     * Uploads a physical file via multipart form data and creates a corresponding file record.
     *
     * @param file the multipart file to upload
     * @return the created {@link FileResponse} containing the stored file's metadata
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('file.upload.own')")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileService.uploadFile(file)));
    }

    /**
     * Downloads a file by its unique identifier as a binary stream attachment.
     *
     * @param id the unique ID of the file to download
     * @return a resource response with appropriate content-type and content-disposition headers
     */
    @GetMapping("/download/{id}")
    @PreAuthorize("hasAuthority('file.read.own') or hasAuthority('file.read.team') or hasAuthority('file.read.all')")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable Long id) {
        com.vortexadmin.entity.File fileEntity = fileService.getFileEntity(id);
        org.springframework.core.io.Resource resource = fileService.downloadFile(fileEntity);

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(fileEntity.getFileType() != null ? fileEntity.getFileType() : "application/octet-stream"))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Renames a file by its unique identifier.
     *
     * @param id   the unique ID of the file to rename
     * @param name the new display name for the file
     * @return a success response with no data payload upon successful rename
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('file.upload.own')")
    public ResponseEntity<ApiResponse<Void>> renameFile(@PathVariable Long id, @RequestParam String name) {
        fileService.renameFile(id, name);
        return ResponseEntity.ok(ApiResponse.success("File renamed", null));
    }

    /**
     * Deletes a file by its unique identifier.
     *
     * @param id the unique ID of the file to delete
     * @return a success response with no data payload upon successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('file.delete.own') or hasAuthority('file.delete.all')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok(ApiResponse.success("File deleted", null));
    }
}
