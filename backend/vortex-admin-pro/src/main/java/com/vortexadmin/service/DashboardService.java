package com.vortexadmin.service;

import com.vortexadmin.dto.response.DashboardDataResponse;

/**
 * Service contract for aggregating and returning all data required to render the main
 * admin dashboard, including KPIs, charts, and recent-activity widgets.
 */
public interface DashboardService {

    /**
     * Collects and returns all dashboard statistics including total and active user counts,
     * revenue figures, growth trends, role distribution, recent audit log entries, and
     * recent user activity.
     *
     * @return a {@link DashboardDataResponse} containing the complete set of dashboard data
     */
    DashboardDataResponse getDashboardStats();
}
