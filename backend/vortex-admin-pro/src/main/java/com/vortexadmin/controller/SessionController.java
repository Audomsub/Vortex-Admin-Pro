package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.SessionResponse;
import com.vortexadmin.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getMySessions() {
        return ResponseEntity.ok(ApiResponse.success("Sessions fetched", sessionService.getMySessions()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(@PathVariable Long id) {
        sessionService.revokeSession(id);
        return ResponseEntity.ok(ApiResponse.success("Session revoked", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> revokeAllOtherSessions() {
        sessionService.revokeAllSessions();
        return ResponseEntity.ok(ApiResponse.success("All sessions revoked", null));
    }
}
