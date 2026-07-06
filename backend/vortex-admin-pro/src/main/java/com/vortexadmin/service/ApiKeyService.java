package com.vortexadmin.service;

import com.vortexadmin.dto.request.CreateApiKeyRequest;
import com.vortexadmin.dto.response.ApiKeyResponse;

import java.util.List;

/**
 * Service contract for API key management, allowing authenticated users to generate, list,
 * and revoke their personal API keys for programmatic access to the platform.
 */
public interface ApiKeyService {

    /**
     * Generates a new API key for the currently authenticated user, stores a hashed version
     * for validation, and returns the response including the plaintext key (shown only once).
     *
     * @param request the key creation payload including a human-readable label and optional
     *                rate-limit configuration
     * @return the newly created API key response, including the raw key value
     */
    ApiKeyResponse createKey(CreateApiKeyRequest request);

    /**
     * Returns all API keys belonging to the currently authenticated user, ordered from newest
     * to oldest.  The plaintext key value is never included in list responses.
     *
     * @return a list of the calling user's API key responses
     */
    List<ApiKeyResponse> getMyKeys();

    /**
     * Permanently revokes (deletes) the specified API key, preventing it from being used for
     * further authentication.
     *
     * @param id the primary key of the API key to revoke
     * @throws com.vortexadmin.exception.ApiException if the key is not found or does not belong
     *         to the currently authenticated user
     */
    void revokeKey(Long id);
}
