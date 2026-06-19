package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.AuditLogResponse;
import com.vortexadmin.service.AuditLogService;
import com.vortexadmin.service.ExportService;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final ExportService exportService;

    @GetMapping
    @PreAuthorize("hasAuthority('audit.read')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched", auditLogService.getCompanyAuditLogs()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('audit.read')")
    public ResponseEntity<byte[]> exportLogs(@RequestParam(defaultValue = "csv") String format) throws Exception {
        List<AuditLogResponse> logs = auditLogService.getCompanyAuditLogs();

        List<String> headers = Arrays.asList("action", "entityname", "entityid", "username", "ipaddress", "details", "createdat");

        List<Map<String, Object>> data = logs.stream().map(log -> {
            Map<String, Object> row = new HashMap<>();
            row.put("action", log.getAction());
            row.put("entityname", log.getEntityName());
            row.put("entityid", log.getEntityId());
            row.put("username", log.getUsername());
            row.put("ipaddress", log.getIpAddress());
            row.put("details", log.getDetails());
            row.put("createdat", log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
            return row;
        }).collect(Collectors.toList());

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
