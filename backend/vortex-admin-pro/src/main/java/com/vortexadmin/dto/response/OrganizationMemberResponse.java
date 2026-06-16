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
public class OrganizationMemberResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
}
