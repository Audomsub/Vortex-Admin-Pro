package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SettingRequest {
    @NotBlank
    private String key;

    @NotBlank
    private String value;
}
