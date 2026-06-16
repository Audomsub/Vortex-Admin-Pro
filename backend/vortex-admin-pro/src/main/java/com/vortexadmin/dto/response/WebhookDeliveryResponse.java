package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WebhookDeliveryResponse {
    private Long id;
    private String eventType;
    private Integer statusCode;
    private boolean success;
    private String responseBody;
    private LocalDateTime deliveredAt;
}
