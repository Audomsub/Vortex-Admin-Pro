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

/**
 * Handles audit log business logic, including retrieval of the most recent company-wide
 * audit entries and persisting new audit log records with optional authenticated-user
 * attribution and real-time SSE broadcast to connected dashboard clients.
 */
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final SseEmitterService sseEmitterService;

    /**
     * Maps an {@link AuditLog} entity to an {@link AuditLogResponse} DTO.
     * If the log has no associated user (e.g. a system-generated log), the username
     * field in the response is set to "System".
     *
     * @param log the audit log entity to map
     * @return the corresponding audit log response DTO
     */
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

    /**
     * Returns up to the 500 most recent audit log entries for the company, ordered by
     * creation time descending, fetched with a joined user query to avoid N+1 selects.
     *
     * @return a list of up to 500 audit log response DTOs
     */
    @Override
    public List<AuditLogResponse> getCompanyAuditLogs() {
        return auditLogRepository.findTop500WithUser(PageRequest.of(0, 500))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Persists a new audit log entry attributed to the currently authenticated user when
     * one is available, or with a {@code null} user for background/system-generated events.
     * After saving, broadcasts a {@code dashboard_update} SSE event with the value "reload"
     * to all connected clients so dashboards refresh in real time.
     *
     * @param action     the action performed (e.g. "LOGIN", "CREATE", "UPDATE", "DELETE")
     * @param entityName the type of entity the action was performed on (e.g. "User", "Task")
     * @param entityId   the id of the affected entity, or {@code null} for non-entity actions
     * @param details    a human-readable description of what happened
     * @param ipAddress  the client IP address at the time of the action, or {@code null}
     */
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
