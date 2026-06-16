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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

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
        User uploader = userRepository.findById(SecurityUtils.getCurrentUserId()).get();

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
    public void renameFile(Long id, String name) {
        File file = fileRepository.findById(id).orElseThrow();
        if (!file.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        file.setFileName(name);
        fileRepository.save(file);
    }

    @Override
    @Transactional
    public void deleteFile(Long id) {
        File file = fileRepository.findById(id).orElseThrow();
        if (!file.getUser().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        fileRepository.delete(file);
    }
}
