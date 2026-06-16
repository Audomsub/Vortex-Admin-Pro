package com.vortexadmin.service;

import com.vortexadmin.dto.request.FileRequest;
import com.vortexadmin.dto.response.FileResponse;

import java.util.List;

public interface FileService {
    List<FileResponse> getMyFiles();
    FileResponse uploadFileRecord(FileRequest request);
    void renameFile(Long id, String name);
    void deleteFile(Long id);
}
