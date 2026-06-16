package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ReportStatsResponse {
    private KpiCards kpis;
    private List<RevenueChart> revenueChart;
    private List<UserGrowthChart> userGrowthChart;

    @Data
    @Builder
    public static class KpiCards {
        private String totalRevenue;
        private String revenueTrend;
        private String activeUsers;
        private String activeUsersTrend;
        private String systemActivity;
        private String activityTrend;
        private String conversionRate;
        private String conversionTrend;
    }

    @Data
    @Builder
    public static class RevenueChart {
        private String name;
        private double revenue;
        private double expenses;
    }

    @Data
    @Builder
    public static class UserGrowthChart {
        private String name;
        private long active;
        private long newUsers;
    }
}
