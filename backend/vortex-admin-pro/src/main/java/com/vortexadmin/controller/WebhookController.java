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

/**
 * Handles HTTP requests for webhook endpoint management, allowing administrators to configure,
 * update, delete, and test webhook integrations, delegating business logic to WebhookService.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Retrieves all registered webhook endpoints for the authenticated user's tenant.
     *
     * @return a list of {@link WebhookEndpointResponse} objects representing configured webhooks
     */
    @GetMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<List<WebhookEndpointResponse>>> getAllEndpoints() {
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoints fetched", webhookService.getAllEndpoints()));
    }

    /**
     * Registers a new webhook endpoint to receive event notifications.
     *
     * @param request the webhook creation payload containing the target URL and subscribed event types
     * @return the created {@link WebhookEndpointResponse} reflecting the persisted endpoint
     */
    @PostMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<WebhookEndpointResponse>> createEndpoint(@Valid @RequestBody WebhookEndpointRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoint created", webhookService.createEndpoint(request)));
    }

    /**
     * Updates an existing webhook endpoint's URL or subscribed event types.
     *
     * @param id      the unique ID of the webhook endpoint to update
     * @param request the update payload containing the new URL and/or event types
     * @return the updated {@link WebhookEndpointResponse} reflecting the changes
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<WebhookEndpointResponse>> updateEndpoint(
            @PathVariable Long id, @Valid @RequestBody WebhookEndpointRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoint updated", webhookService.updateEndpoint(id, request)));
    }

    /**
     * Deletes a webhook endpoint by its unique identifier.
     *
     * @param id the unique ID of the webhook endpoint to delete
     * @return a success response with no data payload upon successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteEndpoint(@PathVariable Long id) {
        webhookService.deleteEndpoint(id);
        return ResponseEntity.ok(ApiResponse.success("Webhook endpoint deleted", null));
    }

    /**
     * Retrieves the delivery history for a specific webhook endpoint.
     *
     * @param id the unique ID of the webhook endpoint whose delivery logs are being requested
     * @return a list of {@link WebhookDeliveryResponse} objects showing past delivery attempts and statuses
     */
    @GetMapping("/{id}/deliveries")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<List<WebhookDeliveryResponse>>> getDeliveries(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Webhook deliveries fetched", webhookService.getDeliveries(id)));
    }

    /**
     * Sends a test event to a webhook endpoint to verify connectivity and handler behavior.
     *
     * @param id the unique ID of the webhook endpoint to test
     * @return a success response with no data payload after the test event is dispatched
     */
    @PostMapping("/{id}/test")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<Void>> sendTestEvent(@PathVariable Long id) {
        webhookService.sendTestEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Test event sent", null));
    }
}
