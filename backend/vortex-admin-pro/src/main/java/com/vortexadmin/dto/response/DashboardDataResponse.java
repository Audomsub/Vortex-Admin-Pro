package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardDataResponse {
    private StatCards statCards;
    private List<ChartData> userGrowthChart;
    private List<ChartData> taskActivityChart;
    private List<ChartData> loginActivityChart;
    private List<DistributionData> roleDistribution;
    private SystemHealth systemHealth;
    private List<ActivityDto> recentActivities;
    private List<UserDto> latestUsers;

    @Data
    @Builder
    public static class StatCards {
        private long totalUsers;
        private String totalUsersTrend;
        private long activeUsers;
        private String activeUsersTrend;
        private long totalTeams;
        private String totalTeamsTrend;
        private long totalTasks;
        private String totalTasksTrend;
        private long totalEvents;
        private String totalEventsTrend;
        private long unreadNotifications;
        private String unreadNotificationsTrend;
    }

    @Data
    @Builder
    public static class ChartData {
        private String name;
        private long users;   // For User Growth (Total registered), Login Activity (Total Logins)
        private long active;  // For User Growth (Active users), Login Activity (Active Sessions)
        private long created; // For Task Activity
        private long completed; // For Task Activity
    }

    @Data
    @Builder
    public static class DistributionData {
        private String name; // Role Name
        private long value;  // Count
    }

    @Data
    @Builder
    public static class SystemHealth {
        private String cpuUsage;
        private String memoryUsage;
        private String storageUsage;
        private String databaseStatus;
    }

    @Data
    @Builder
    public static class ActivityDto {
        private String title;
        private String desc;
        private String time;
        private String type; // 'primary', 'success', 'warning', 'danger'
    }

    @Data
    @Builder
    public static class UserDto {
        private String id;
        private String username;
        private String email;
        private String status;
        private String avatarText;
    }
}
