package com.vortexadmin.service;

import java.util.List;
import java.util.Map;

public interface AiService {
    String analyzeAuditLogs(List<com.vortexadmin.dto.response.AuditLogResponse> logs);
}
