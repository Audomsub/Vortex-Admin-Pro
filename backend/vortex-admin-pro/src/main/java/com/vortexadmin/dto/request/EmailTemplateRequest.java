package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailTemplateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String subject;

    @NotBlank
    private String content;
}
