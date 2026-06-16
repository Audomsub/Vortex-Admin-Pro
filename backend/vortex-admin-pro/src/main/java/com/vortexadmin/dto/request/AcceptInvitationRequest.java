package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcceptInvitationRequest {

    @NotBlank(message = "Invitation token is required")
    private String token;
}
