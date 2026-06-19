package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze-logs")
    @PreAuthorize("hasAuthority('audit.read')")
    public ResponseEntity<ApiResponse<String>> analyzeLogs(@RequestBody List<com.vortexadmin.dto.response.AuditLogResponse> logs) {
        String analysis = aiService.analyzeAuditLogs(logs);
        return ResponseEntity.ok(ApiResponse.success("AI Analysis Complete", analysis));
    }
}
