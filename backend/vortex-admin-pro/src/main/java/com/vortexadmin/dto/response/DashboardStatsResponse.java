package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalUsers;
    private long totalProjects;
    private long totalTasks;
    private long unreadNotifications;
}
