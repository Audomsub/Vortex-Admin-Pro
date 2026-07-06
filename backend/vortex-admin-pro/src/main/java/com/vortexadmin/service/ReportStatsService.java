package com.vortexadmin.service;

import com.vortexadmin.dto.response.ReportStatsResponse;

/**
 * Service contract for computing and returning aggregated report statistics over a configurable
 * time frame, used by both the reporting UI and the scheduled email report service.
 */
public interface ReportStatsService {

    /**
     * Returns aggregated KPIs and trend data for the specified time frame.
     *
     * @param timeframe the reporting window; supported values include {@code "7D"} (last 7 days),
     *                  {@code "1M"} (last month), {@code "3M"} (last 3 months), and
     *                  {@code "1Y"} (last year)
     * @return a {@link ReportStatsResponse} containing KPI metrics and time-series chart data
     *         for the requested period
     */
    ReportStatsResponse getReportStats(String timeframe);
}
