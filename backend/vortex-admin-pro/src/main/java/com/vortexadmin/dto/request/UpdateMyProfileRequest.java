package com.vortexadmin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateMyProfileRequest {
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;
}
