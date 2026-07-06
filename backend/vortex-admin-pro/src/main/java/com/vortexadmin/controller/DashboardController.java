package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.DashboardDataResponse;
import com.vortexadmin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles HTTP requests for the admin dashboard, exposing aggregated statistics
 * and analytics data by delegating to DashboardService.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Retrieves aggregated dashboard statistics including user counts, activity metrics,
     * revenue data, and chart series for the authenticated user's tenant.
     *
     * @return a {@link DashboardDataResponse} containing all dashboard KPIs and chart data
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<DashboardDataResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched", dashboardService.getDashboardStats()));
    }
}
