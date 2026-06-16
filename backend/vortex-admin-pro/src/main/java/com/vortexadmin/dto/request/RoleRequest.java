package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class RoleRequest {
    @NotBlank
    private String name;

    private String description;

    private Set<Long> permissionIds;
}
