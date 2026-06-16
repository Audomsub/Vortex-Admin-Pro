package com.vortexadmin.controller;

import com.vortexadmin.dto.request.FileRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.FileResponse;
import com.vortexadmin.service.FileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

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
