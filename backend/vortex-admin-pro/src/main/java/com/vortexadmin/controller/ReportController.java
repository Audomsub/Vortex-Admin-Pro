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

/**
 * Handles HTTP requests for reporting, providing statistical summaries and
 * downloadable report exports, delegating to ReportStatsService and ReportExportService.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportExportService reportExportService;
    private final ReportStatsService reportStatsService;

    /**
     * Retrieves aggregated report statistics for the specified time frame.
     *
     * @param timeframe the reporting window (e.g., {@code "7D"}, {@code "30D"}, {@code "1Y"}); defaults to {@code "7D"}
     * @return a {@link ReportStatsResponse} containing KPIs and trend data for the given period
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('report.view')")
    public ResponseEntity<ApiResponse<ReportStatsResponse>> getStats(@RequestParam(defaultValue = "7D") String timeframe) {
        return ResponseEntity.ok(ApiResponse.success("Report stats fetched", reportStatsService.getReportStats(timeframe)));
    }

    /**
     * Exports a named report as a downloadable file in the specified format.
     *
     * @param reportType the type/name of the report to export (e.g., {@code "users"}, {@code "revenue"})
     * @param format     the output format, either {@code "csv"} (default) or {@code "excel"}
     * @return a byte array response with appropriate content-type and content-disposition headers
     */
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
