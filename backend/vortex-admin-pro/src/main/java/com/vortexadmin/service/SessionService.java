package com.vortexadmin.service;

import com.vortexadmin.dto.response.SessionResponse;

import java.util.List;

/**
 * Service contract for managing the login sessions of the currently authenticated user,
 * including retrieval and selective or bulk revocation.
 */
public interface SessionService {

    /**
     * Returns all sessions (both active and historical) for the currently authenticated user,
     * ordered from most recent to oldest.
     *
     * @return a list of session responses for the calling user
     */
    List<SessionResponse> getMySessions();

    /**
     * Revokes (terminates) the session with the specified ID, provided it belongs to the
     * currently authenticated user.
     *
     * @param id the primary key of the session to revoke
     * @throws com.vortexadmin.exception.ApiException if the session is not found or does not
     *         belong to the calling user
     */
    void revokeSession(Long id);

    /**
     * Revokes all active sessions for the currently authenticated user, effectively signing
     * them out of all devices simultaneously.
     */
    void revokeAllSessions();
}
