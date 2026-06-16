package com.vortexadmin.service;

import com.vortexadmin.dto.response.ReportStatsResponse;

public interface ReportStatsService {
    ReportStatsResponse getReportStats(String timeframe);
}
