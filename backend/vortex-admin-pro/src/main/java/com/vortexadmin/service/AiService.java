package com.vortexadmin.service;

/**
 * Service contract for AI-powered analytics operations, providing automated insights derived
 * from system data such as audit logs.
 */
public interface AiService {

    /**
     * Analyzes recent audit log entries using an AI/LLM service and returns a plain-text
     * summary of notable patterns, anomalies, or security observations.
     *
     * @return a natural-language analysis of the recent audit log activity
     */
    String analyzeAuditLogs();
}
