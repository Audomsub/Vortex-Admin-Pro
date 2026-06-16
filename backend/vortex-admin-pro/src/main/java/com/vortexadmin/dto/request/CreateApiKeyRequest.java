package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateApiKeyRequest {

    @NotBlank(message = "Key name is required")
    @Size(max = 100, message = "Key name must be at most 100 characters")
    private String name;

    // Optional: number of days until the key expires (null = never)
    private Integer expiresInDays;
}
