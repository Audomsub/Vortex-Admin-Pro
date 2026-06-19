package com.vortexadmin.controller;

import com.vortexadmin.dto.request.WebhookEndpointRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.WebhookDeliveryResponse;
import com.vortexadmin.dto.response.WebhookEndpointResponse;
import com.vortexadmin.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @GetMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<List<WebhookEndpointResponse>>> getAllEndpoints() {
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoints fetched", webhookService.getAllEndpoints()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<WebhookEndpointResponse>> createEndpoint(@Valid @RequestBody WebhookEndpointRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoint created", webhookService.createEndpoint(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<WebhookEndpointResponse>> updateEndpoint(
            @PathVariable Long id, @Valid @RequestBody WebhookEndpointRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoint updated", webhookService.updateEndpoint(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteEndpoint(@PathVariable Long id) {
        webhookService.deleteEndpoint(id);
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoint deleted", null));
    }

    @GetMapping("/{id}/deliveries")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<List<WebhookDeliveryResponse>>> getDeliveries(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Webhook deliveries fetched", webhookService.getDeliveries(id)));
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<Void>> sendTestEvent(@PathVariable Long id) {
        webhookService.sendTestEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Test event sent", null));
    }
}
