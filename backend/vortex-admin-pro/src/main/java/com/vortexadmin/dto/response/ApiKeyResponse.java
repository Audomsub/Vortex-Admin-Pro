package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiKeyResponse {
    private Long id;
    private String name;
    private String prefix;
    private boolean revoked;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    // Only populated once, immediately after creation
    private String fullKey;
}
