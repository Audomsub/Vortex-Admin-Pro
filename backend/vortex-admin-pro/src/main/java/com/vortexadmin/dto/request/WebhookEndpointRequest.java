package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class WebhookEndpointRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.+", message = "URL must start with http:// or https://")
    private String url;

    @NotEmpty(message = "At least one event must be selected")
    private List<String> events;

    private boolean active = true;
}
