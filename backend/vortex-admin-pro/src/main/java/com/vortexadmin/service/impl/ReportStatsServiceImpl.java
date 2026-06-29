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

@Service
@RequiredArgsConstructor
public class ReportStatsServiceImpl implements ReportStatsService {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;

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

    private String formatRevenueTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }
        BigDecimal change = current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
        return (change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + change + "%";
    }

    private String formatTrend(long current, long previous) {
        if (previous == 0) return current > 0 ? "+100%" : "0%";
        double change = ((double)(current - previous) / previous) * 100;
        return String.format("%s%.1f%%", change >= 0 ? "+" : "", change);
    }
}
