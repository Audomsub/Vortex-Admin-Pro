package com.vortexadmin.service;

import com.vortexadmin.dto.response.AuditLogResponse;

import java.util.List;

public interface AuditLogService {
    List<AuditLogResponse> getCompanyAuditLogs();
    void logAction(String action, String entityName, Long entityId, String details, String ipAddress);
}
