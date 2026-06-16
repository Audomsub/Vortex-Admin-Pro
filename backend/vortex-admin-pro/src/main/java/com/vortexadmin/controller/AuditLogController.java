package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.AuditLogResponse;
import com.vortexadmin.service.AuditLogService;
import com.vortexadmin.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ExportService exportService;

    @GetMapping
    @PreAuthorize("hasAuthority('audit.read')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched", auditLogService.getCompanyAuditLogs()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('audit.read')")
    public ResponseEntity<byte[]> exportLogs(@RequestParam(defaultValue = "csv") String format) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> l1 = new HashMap<>(); l1.put("action", "LOGIN"); l1.put("username", "admin"); l1.put("details", "User logged in");
        data.add(l1);
        
        List<String> headers = Arrays.asList("action", "username", "details");
        
        byte[] bytes;
        String contentType;
        String filename;
        
        if ("excel".equalsIgnoreCase(format)) {
            bytes = exportService.exportToExcel(data, headers);
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filename = "audit_logs.xlsx";
        } else {
            bytes = exportService.exportToCsv(data, headers);
            contentType = "text/csv";
            filename = "audit_logs.csv";
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
