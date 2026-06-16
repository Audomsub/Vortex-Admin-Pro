package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WebhookEndpointResponse {
    private Long id;
    private String name;
    private String url;
    private List<String> events;
    private boolean active;
    private LocalDateTime createdAt;

    // Only populated once, immediately after creation
    private String secret;
}
