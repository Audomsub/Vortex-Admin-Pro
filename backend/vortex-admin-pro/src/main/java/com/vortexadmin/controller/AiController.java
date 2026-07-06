package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Handles HTTP requests for AI-powered analytics features, currently providing
 * intelligent audit log analysis, delegating processing to AiService.
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * Analyzes the tenant's audit logs using AI to surface anomalies, patterns, or security insights.
     *
     * @return a plain-text AI-generated analysis summary of the recent audit log activity
     */
    @PostMapping("/analyze-logs")
    @PreAuthorize("hasAuthority('audit.read')")
    public ResponseEntity<ApiResponse<String>> analyzeLogs() {
        String analysis = aiService.analyzeAuditLogs();
        return ResponseEntity.ok(ApiResponse.success("AI Analysis Complete", analysis));
    }
}
