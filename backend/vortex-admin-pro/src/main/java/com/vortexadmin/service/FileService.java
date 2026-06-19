package com.vortexadmin.service;

import com.vortexadmin.dto.request.FileRequest;
import com.vortexadmin.dto.response.FileResponse;
import com.vortexadmin.entity.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    List<FileResponse> getMyFiles();
    FileResponse uploadFileRecord(FileRequest request);
    FileResponse uploadFile(MultipartFile file);
    Resource downloadFile(File fileEntity);
    File getFileEntity(Long id);
    void renameFile(Long id, String name);
    void deleteFile(Long id);
}
