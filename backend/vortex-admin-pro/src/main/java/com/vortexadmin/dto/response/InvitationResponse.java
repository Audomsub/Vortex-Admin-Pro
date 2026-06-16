package com.vortexadmin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private String email;
    private String token;
    private String role;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
