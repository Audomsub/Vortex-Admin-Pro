package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze-logs")
    @PreAuthorize("hasAuthority('audit.read')")
    public ResponseEntity<ApiResponse<String>> analyzeLogs() {
        String analysis = aiService.analyzeAuditLogs();
        return ResponseEntity.ok(ApiResponse.success("AI Analysis Complete", analysis));
    }
}
