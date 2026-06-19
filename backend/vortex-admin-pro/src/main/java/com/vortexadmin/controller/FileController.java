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

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping
    @PreAuthorize("hasAuthority('file.read.own') or hasAuthority('file.read.team') or hasAuthority('file.read.all')")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getMyFiles() {
        return ResponseEntity.ok(ApiResponse.success("Files fetched", fileService.getMyFiles()));
    }

    @PostMapping("/upload-record")
    @PreAuthorize("hasAuthority('file.upload.own')")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFileRecord(@Valid @RequestBody FileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("File record created", fileService.uploadFileRecord(request)));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('file.upload.own')")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileService.uploadFile(file)));
    }

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

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('file.upload.own')")
    public ResponseEntity<ApiResponse<Void>> renameFile(@PathVariable Long id, @RequestParam String name) {
        fileService.renameFile(id, name);
        return ResponseEntity.ok(ApiResponse.success("File renamed", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('file.delete.own') or hasAuthority('file.delete.all')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok(ApiResponse.success("File deleted", null));
    }
}
