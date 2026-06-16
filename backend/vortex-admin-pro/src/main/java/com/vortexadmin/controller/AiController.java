package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/analyze-logs")
    @PreAuthorize("hasAuthority('audit.read')")
    public ResponseEntity<ApiResponse<String>> analyzeLogs(@RequestBody List<Map<String, Object>> logs) {
        String analysis = aiService.analyzeAuditLogs(logs);
        return ResponseEntity.ok(ApiResponse.success("AI Analysis Complete", analysis));
    }
}
