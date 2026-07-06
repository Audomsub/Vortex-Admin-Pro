package com.vortexadmin.service;

import com.vortexadmin.dto.response.AuditLogResponse;

import java.util.List;

/**
 * Service contract for audit logging operations, providing retrieval of audit trails and
 * programmatic creation of log entries for significant system events.
 */
public interface AuditLogService {

    /**
     * Returns all audit log entries for the company/tenant of the currently authenticated user,
     * ordered from newest to oldest.
     *
     * @return a list of audit log responses for the caller's tenant
     */
    List<AuditLogResponse> getCompanyAuditLogs();

    /**
     * Records an audit log entry for an action performed on a domain entity.
     *
     * @param action     a short description of the action performed (e.g., "USER_CREATED",
     *                   "ROLE_UPDATED")
     * @param entityName the name of the entity type affected (e.g., "User", "Role")
     * @param entityId   the primary key of the affected entity, or {@code null} if not applicable
     * @param details    additional free-text context about the action
     * @param ipAddress  the IP address of the client that triggered the action
     */
    void logAction(String action, String entityName, Long entityId, String details, String ipAddress);
}
