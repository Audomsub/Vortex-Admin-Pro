package com.vortexadmin.controller;

import com.vortexadmin.dto.request.CreateApiKeyRequest;
import com.vortexadmin.dto.response.ApiKeyResponse;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for API key management, allowing authorized users to create,
 * list, and revoke programmatic access keys, delegating business logic to ApiKeyService.
 */
@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    /**
     * Creates a new API key for the authenticated user with the specified label and permissions.
     *
     * @param request the API key creation payload containing the key name and optional scopes
     * @return the created {@link ApiKeyResponse} containing the generated key value (shown only once)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> createKey(@Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("API key created", apiKeyService.createKey(request)));
    }

    /**
     * Retrieves all API keys belonging to the authenticated user.
     *
     * @return a list of {@link ApiKeyResponse} objects; the plain-text key is not included for security
     */
    @GetMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> getMyKeys() {
        return ResponseEntity.ok(ApiResponse.success("API keys fetched", apiKeyService.getMyKeys()));
    }

    /**
     * Revokes and deletes an API key by its unique identifier.
     *
     * @param id the unique ID of the API key to revoke
     * @return a success response with no data payload upon successful revocation
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<Void>> revokeKey(@PathVariable Long id) {
        apiKeyService.revokeKey(id);
        return ResponseEntity.ok(ApiResponse.success("API key revoked", null));
    }
}
