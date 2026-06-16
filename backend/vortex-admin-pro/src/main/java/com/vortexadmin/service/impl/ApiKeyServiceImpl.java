package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.CreateApiKeyRequest;
import com.vortexadmin.dto.response.ApiKeyResponse;
import com.vortexadmin.entity.ApiKey;
import com.vortexadmin.entity.User;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.ApiKeyRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.ApiKeyService;
import com.vortexadmin.util.ApiKeyUtils;
import com.vortexadmin.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser() {
        return userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ApiKeyResponse mapToResponse(ApiKey key) {
        return ApiKeyResponse.builder()
                .id(key.getId())
                .name(key.getName())
                .prefix(key.getPrefix())
                .revoked(key.isRevoked())
                .lastUsedAt(key.getLastUsedAt())
                .expiresAt(key.getExpiresAt())
                .createdAt(key.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public ApiKeyResponse createKey(CreateApiKeyRequest request) {
        User user = currentUser();

        String fullKey = ApiKeyUtils.generateKey();

        ApiKey apiKey = ApiKey.builder()
                .name(request.getName())
                .prefix(ApiKeyUtils.extractPrefix(fullKey))
                .keyHash(ApiKeyUtils.hash(fullKey))
                .revoked(false)
                .expiresAt(request.getExpiresInDays() != null
                        ? LocalDateTime.now().plusDays(request.getExpiresInDays())
                        : null)
                .user(user)
                .build();
        apiKeyRepository.save(apiKey);

        ApiKeyResponse response = mapToResponse(apiKey);
        response.setFullKey(fullKey); // shown to the user only once
        return response;
    }

    @Override
    public List<ApiKeyResponse> getMyKeys() {
        return apiKeyRepository.findByUserOrderByCreatedAtDesc(currentUser()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeKey(Long id) {
        ApiKey apiKey = apiKeyRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "API key not found"));
        apiKey.setRevoked(true);
        apiKeyRepository.save(apiKey);
    }
}
