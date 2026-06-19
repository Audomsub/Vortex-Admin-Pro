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

@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> createKey(@Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("API key created", apiKeyService.createKey(request)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> getMyKeys() {
        return ResponseEntity.ok(ApiResponse.success("API keys fetched", apiKeyService.getMyKeys()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('settings.manage')")
    public ResponseEntity<ApiResponse<Void>> revokeKey(@PathVariable Long id) {
        apiKeyService.revokeKey(id);
        return ResponseEntity.ok(ApiResponse.success("API key revoked", null));
    }
}
