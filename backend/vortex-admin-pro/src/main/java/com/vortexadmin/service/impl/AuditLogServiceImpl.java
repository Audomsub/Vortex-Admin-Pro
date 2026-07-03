package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.AuditLogResponse;
import com.vortexadmin.entity.AuditLog;
import com.vortexadmin.entity.User;
import com.vortexadmin.repository.AuditLogRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.AuditLogService;
import com.vortexadmin.service.SseEmitterService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final SseEmitterService sseEmitterService;

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityName(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .username(log.getUser() != null ? log.getUser().getUsername() : "System")
                .createdAt(log.getCreatedAt())
                .build();
    }

    @Override
    public List<AuditLogResponse> getCompanyAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 500))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void logAction(String action, String entityName, Long entityId, String details, String ipAddress) {
        User user = null;
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            user = userRepository.findById(currentUserId).orElse(null);
        } catch (Exception e) {
            // Context might be missing in some system jobs
        }

        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityName)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .user(user)
                .build();
        auditLogRepository.save(log);

        sseEmitterService.broadcast("dashboard_update", "reload");
    }
}
