package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.ReportStatsResponse;
import com.vortexadmin.entity.Invoice;
import com.vortexadmin.repository.InvoiceRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.ReportStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles report statistics business logic by aggregating KPI metrics (revenue, active users),
 * building time-series revenue charts, and generating user growth charts across configurable
 * timeframes, with daily granularity for short ranges and monthly granularity for longer ones.
 */
@Service
@RequiredArgsConstructor
public class ReportStatsServiceImpl implements ReportStatsService {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Computes and returns the full report statistics for the given timeframe.
     * Supported timeframes: "7D" (default, 7 days daily), "1M"/"30D" (30 days daily),
     * "3M" (3 months monthly), "1Y" (1 year monthly), "ALL" (5 years monthly).
     * Short timeframes use daily granularity for charts; longer timeframes use monthly.
     * Revenue is summed from PAID invoices; expenses are estimated at 40% of revenue.
     * Active user counts and new user counts are compared against the equivalent previous
     * period to compute trend percentages.
     *
     * @param timeframe the report timeframe string (e.g. "7D", "1M", "3M", "1Y", "ALL")
     * @return a {@link ReportStatsResponse} containing KPI cards, revenue chart data,
     *         and user growth chart data for the requested timeframe
     */
    @Override
    public ReportStatsResponse getReportStats(String timeframe) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        boolean groupDaily = true;

        switch (timeframe != null ? timeframe.toUpperCase() : "7D") {
            case "1M", "30D" -> startDate = now.minusDays(30);
            case "3M" -> { startDate = now.minusMonths(3); groupDaily = false; }
            case "1Y" -> { startDate = now.minusYears(1); groupDaily = false; }
            case "ALL" -> { startDate = now.minusYears(5); groupDaily = false; }
            default -> startDate = now.minusDays(7);
        }

        long periodLength = java.time.Duration.between(startDate, now).toDays();
        LocalDateTime prevStart = startDate.minusDays(periodLength);

        // Active users
        long activeUsers = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNull("Active");
        long newUsers = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween("Active", startDate, now);
        long prevActiveUsers = userRepository.countByDeletedAtIsNullAndCreatedAtLessThanEqual(prevStart);

        String activeUsersTrend = formatTrend(activeUsers, prevActiveUsers > 0 ? prevActiveUsers : activeUsers - newUsers);

        // Revenue
        BigDecimal totalRev = invoiceRepository.sumPaidAmountSince(startDate);
        if (totalRev == null) totalRev = BigDecimal.ZERO;

        BigDecimal prevRev = invoiceRepository.sumPaidAmountBetween(prevStart, startDate);
        if (prevRev == null) prevRev = BigDecimal.ZERO;

        String revenueTrend = formatRevenueTrend(totalRev, prevRev);

        ReportStatsResponse.KpiCards kpis = ReportStatsResponse.KpiCards.builder()
                .totalRevenue("$" + String.format("%,.2f", totalRev.doubleValue()))
                .revenueTrend(revenueTrend)
                .activeUsers(String.format("%,d", activeUsers))
                .activeUsersTrend(activeUsersTrend)
                .systemActivity("N/A")
                .activityTrend("N/A")
                .conversionRate("N/A")
                .conversionTrend("N/A")
                .build();

        // Revenue chart
        List<Invoice> invoices = invoiceRepository.findByStatusAndIssuedAtBetweenOrderByIssuedAtAsc("PAID", startDate, now);

        java.util.Map<String, Double> revenueMap = new java.util.LinkedHashMap<>();
        if (groupDaily) {
            for (int i = 0; !startDate.plusDays(i).toLocalDate().isAfter(now.toLocalDate()); i++) {
                revenueMap.put(startDate.plusDays(i).format(DateTimeFormatter.ofPattern("MMM dd")), 0.0);
            }
            for (Invoice inv : invoices) {
                String key = inv.getIssuedAt().format(DateTimeFormatter.ofPattern("MMM dd"));
                revenueMap.put(key, revenueMap.getOrDefault(key, 0.0) + inv.getAmount().doubleValue());
            }
        } else {
            for (int i = 0; !startDate.plusMonths(i).withDayOfMonth(1).toLocalDate().isAfter(now.toLocalDate()); i++) {
                revenueMap.put(startDate.plusMonths(i).format(DateTimeFormatter.ofPattern("MMM yyyy")), 0.0);
            }
            for (Invoice inv : invoices) {
                String key = inv.getIssuedAt().format(DateTimeFormatter.ofPattern("MMM yyyy"));
                revenueMap.put(key, revenueMap.getOrDefault(key, 0.0) + inv.getAmount().doubleValue());
            }
        }

        List<ReportStatsResponse.RevenueChart> revChart = new ArrayList<>();
        for (java.util.Map.Entry<String, Double> entry : revenueMap.entrySet()) {
            revChart.add(ReportStatsResponse.RevenueChart.builder()
                    .name(entry.getKey())
                    .revenue(entry.getValue())
                    .expenses(entry.getValue() * 0.4)
                    .build());
        }

        // User growth chart
        List<ReportStatsResponse.UserGrowthChart> userChart = new ArrayList<>();
        if (groupDaily) {
            long days = now.toLocalDate().toEpochDay() - startDate.toLocalDate().toEpochDay();
            for (int i = 0; i <= days; i++) {
                LocalDateTime dayStart = startDate.plusDays(i).withHour(0).withMinute(0);
                LocalDateTime dayEnd = dayStart.plusDays(1);
                String name = dayStart.format(DateTimeFormatter.ofPattern("EEE"));
                long newU = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween("Active", dayStart, dayEnd);
                long act = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtLessThanEqual("Active", dayEnd);
                userChart.add(ReportStatsResponse.UserGrowthChart.builder()
                        .name(name).active(act).newUsers(newU).build());
            }
        } else {
            for (int i = 0; !startDate.plusMonths(i).withDayOfMonth(1).toLocalDate().isAfter(now.toLocalDate()); i++) {
                LocalDateTime monthStart = startDate.plusMonths(i).withDayOfMonth(1).withHour(0);
                LocalDateTime monthEnd = monthStart.plusMonths(1);
                String name = monthStart.format(DateTimeFormatter.ofPattern("MMM"));
                long newU = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween("Active", monthStart, monthEnd);
                long act = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtLessThanEqual("Active", monthEnd);
                userChart.add(ReportStatsResponse.UserGrowthChart.builder()
                        .name(name).active(act).newUsers(newU).build());
            }
        }

        return ReportStatsResponse.builder()
                .kpis(kpis)
                .revenueChart(revChart)
                .userGrowthChart(userChart)
                .build();
    }

    /**
     * Calculates a signed percentage trend string comparing two {@link BigDecimal} revenue
     * values. Returns "+100%" when the previous value is zero but the current is positive,
     * "0%" when both are zero, and a signed one-decimal-place percentage otherwise.
     *
     * @param current  the revenue for the current period
     * @param previous the revenue for the comparison period
     * @return a signed percentage string such as "+12.5%" or "-3.0%"
     */
    private String formatRevenueTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }
        BigDecimal change = current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
        return (change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + change + "%";
    }

    /**
     * Calculates a signed percentage trend string comparing two {@code long} counts.
     * Returns "+100%" when the previous count is zero but the current is positive,
     * "0%" when the previous is zero and there is no growth, and a signed one-decimal-place
     * percentage otherwise.
     *
     * @param current  the count for the current period
     * @param previous the count for the comparison period
     * @return a signed percentage string such as "+25.0%" or "-10.0%"
     */
    private String formatTrend(long current, long previous) {
        if (previous == 0) return current > 0 ? "+100%" : "0%";
        double change = ((double)(current - previous) / previous) * 100;
        return String.format("%s%.1f%%", change >= 0 ? "+" : "", change);
    }
}
