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
public class OrganizationResponse {
    private Long id;
    private String name;
    private String slug;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;
    private String planType;
    private Long ownerId;
    private String ownerName;
    private long memberCount;
    private String currentUserRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
