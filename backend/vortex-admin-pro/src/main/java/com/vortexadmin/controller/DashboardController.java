package com.vortexadmin.controller;

import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.DashboardDataResponse;
import com.vortexadmin.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('dashboard.view')")
    public ResponseEntity<ApiResponse<DashboardDataResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched", dashboardService.getDashboardStats()));
    }
}
