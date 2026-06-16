package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileRequest {
    @NotBlank
    private String fileName;
    
    @NotBlank
    private String fileUrl; // Mock upload logic: UI sends url after direct S3 upload
    
    private String fileType;
    private Long size;
    private Long folderId;
}
