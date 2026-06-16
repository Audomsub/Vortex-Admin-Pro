package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileResponse {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long size;
    private Long folderId;
    private String uploadedByUsername;
    private LocalDateTime createdAt;
}
