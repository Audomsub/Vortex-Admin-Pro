package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorVerifyRequest {

    @NotBlank(message = "Verification code is required")
    private String code;
}
