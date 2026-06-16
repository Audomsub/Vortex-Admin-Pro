package com.vortexadmin.service;

import com.vortexadmin.dto.request.CreateApiKeyRequest;
import com.vortexadmin.dto.response.ApiKeyResponse;

import java.util.List;

public interface ApiKeyService {
    ApiKeyResponse createKey(CreateApiKeyRequest request);
    List<ApiKeyResponse> getMyKeys();
    void revokeKey(Long id);
}
