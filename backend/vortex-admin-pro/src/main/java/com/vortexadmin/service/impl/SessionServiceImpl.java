package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.SessionResponse;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.UserSession;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.repository.UserSessionRepository;
import com.vortexadmin.service.SessionService;
import com.vortexadmin.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser() {
        return userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public List<SessionResponse> getMySessions() {
        return userSessionRepository.findByUserOrderByLoginAtDesc(currentUser()).stream()
                .map(s -> SessionResponse.builder()
                        .id(s.getId())
                        .ipAddress(s.getIpAddress())
                        .userAgent(s.getUserAgent())
                        .loginAt(s.getLoginAt())
                        .logoutAt(s.getLogoutAt())
                        .active(s.getLogoutAt() == null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeSession(Long id) {
        UserSession session = userSessionRepository.findByIdAndUser(id, currentUser())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Session not found"));
        if (session.getLogoutAt() == null) {
            session.setLogoutAt(LocalDateTime.now());
            userSessionRepository.save(session);
        }
    }

    @Override
    @Transactional
    public void revokeAllSessions() {
        List<UserSession> sessions = userSessionRepository.findByUserOrderByLoginAtDesc(currentUser());
        LocalDateTime now = LocalDateTime.now();
        for (UserSession session : sessions) {
            if (session.getLogoutAt() == null) {
                session.setLogoutAt(now);
            }
        }
        userSessionRepository.saveAll(sessions);
    }
}
