package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String action;
    private String entityName;
    private Long entityId;
    private String details;
    private String ipAddress;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
}
