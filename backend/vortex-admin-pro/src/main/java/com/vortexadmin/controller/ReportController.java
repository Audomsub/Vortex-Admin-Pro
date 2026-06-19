package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.ExportFileResponse;
import com.vortexadmin.dto.response.ReportStatsResponse;
import com.vortexadmin.service.ReportExportService;
import com.vortexadmin.service.ReportStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportExportService reportExportService;
    private final ReportStatsService reportStatsService;

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
