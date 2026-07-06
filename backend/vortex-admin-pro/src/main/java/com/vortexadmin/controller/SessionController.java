package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.SessionResponse;
import com.vortexadmin.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for user session management, enabling users to view and
 * revoke their active sessions, delegating business logic to SessionService.
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * Retrieves all active sessions for the currently authenticated user.
     *
     * @return a list of {@link SessionResponse} objects representing the user's open sessions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getMySessions() {
        return ResponseEntity.ok(ApiResponse.success("Sessions fetched", sessionService.getMySessions()));
    }

    /**
     * Revokes a specific session by its unique identifier, effectively logging out that device.
     *
     * @param id the unique ID of the session to revoke
     * @return a success response with no data payload upon successful revocation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(@PathVariable Long id) {
        sessionService.revokeSession(id);
        return ResponseEntity.ok(ApiResponse.success("Session revoked", null));
    }

    /**
     * Revokes all active sessions for the authenticated user except the current one.
     *
     * @return a success response with no data payload upon successful revocation of all sessions
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> revokeAllOtherSessions() {
        sessionService.revokeAllSessions();
        return ResponseEntity.ok(ApiResponse.success("All sessions revoked", null));
    }
}
