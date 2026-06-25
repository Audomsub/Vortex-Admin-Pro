package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.ReportStatsResponse;
import com.vortexadmin.entity.Invoice;
import com.vortexadmin.entity.User;
import com.vortexadmin.repository.InvoiceRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.ReportStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            case "1M":
            case "30D":
                startDate = now.minusDays(30);
                break;
            case "3M":
                startDate = now.minusMonths(3);
                groupDaily = false;
                break;
            case "1Y":
                startDate = now.minusYears(1);
                groupDaily = false;
                break;
            case "ALL":
                startDate = now.minusYears(5);
                groupDaily = false;
                break;
            case "7D":
            default:
                startDate = now.minusDays(7);
                break;
        }

        long activeUsers = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNull("Active");
        long newUsers = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween("Active", startDate, now);

        BigDecimal totalRev = invoiceRepository.sumPaidAmountSince(startDate);
        if (totalRev == null) totalRev = BigDecimal.ZERO;

        ReportStatsResponse.KpiCards kpis = ReportStatsResponse.KpiCards.builder()
                .totalRevenue("$" + String.format("%,.2f", totalRev.doubleValue()))
                .revenueTrend("+5.0%")
                .activeUsers(String.format("%,d", activeUsers))
                .activeUsersTrend("+" + newUsers)
                .systemActivity("98.5%")
                .activityTrend("+0.2%")
                .conversionRate("4.8%")
                .conversionTrend("+1.1%")
                .build();

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
                        .name(name)
                        .active(act)
                        .newUsers(newU)
                        .build());
            }
        } else {
            for (int i = 0; !startDate.plusMonths(i).withDayOfMonth(1).toLocalDate().isAfter(now.toLocalDate()); i++) {
                LocalDateTime monthStart = startDate.plusMonths(i).withDayOfMonth(1).withHour(0);
                LocalDateTime monthEnd = monthStart.plusMonths(1);
                String name = monthStart.format(DateTimeFormatter.ofPattern("MMM"));
                long newU = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween("Active", monthStart, monthEnd);
                long act = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtLessThanEqual("Active", monthEnd);

                userChart.add(ReportStatsResponse.UserGrowthChart.builder()
                        .name(name)
                        .active(act)
                        .newUsers(newU)
                        .build());
            }
        }

        return ReportStatsResponse.builder()
                .kpis(kpis)
                .revenueChart(revChart)
                .userGrowthChart(userChart)
                .build();
    }
}
