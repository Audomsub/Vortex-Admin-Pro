package com.vortexadmin.service;

import com.vortexadmin.dto.response.SessionResponse;

import java.util.List;

public interface SessionService {
    List<SessionResponse> getMySessions();
    void revokeSession(Long id);
    void revokeAllSessions();
}
