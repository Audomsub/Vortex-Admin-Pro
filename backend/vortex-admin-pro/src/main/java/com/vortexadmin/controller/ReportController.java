package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.ExportFileResponse;
import com.vortexadmin.dto.response.ReportStatsResponse;
import com.vortexadmin.service.ReportExportService;
import com.vortexadmin.service.ReportStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportExportService reportExportService;

    @Autowired
    private ReportStatsService reportStatsService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('report.view')")
    public ResponseEntity<ApiResponse<ReportStatsResponse>> getStats(@RequestParam(defaultValue = "7D") String timeframe) {
        return ResponseEntity.ok(ApiResponse.success("Report stats fetched", reportStatsService.getReportStats(timeframe)));
    }

    @GetMapping("/{reportType}/export")
    @PreAuthorize("hasAuthority('report.export')")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable String reportType,
            @RequestParam(defaultValue = "csv") String format) {

        ExportFileResponse file = reportExportService.export(reportType, format);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getContent());
    }
}
