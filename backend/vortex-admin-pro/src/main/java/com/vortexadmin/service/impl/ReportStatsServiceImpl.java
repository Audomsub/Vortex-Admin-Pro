package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.ReportStatsResponse;
import com.vortexadmin.entity.Invoice;
import com.vortexadmin.entity.User;
import com.vortexadmin.repository.InvoiceRepository;
import com.vortexadmin.repository.UserRepository;
import com.vortexadmin.service.ReportStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportStatsServiceImpl implements ReportStatsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public ReportStatsResponse getReportStats(String timeframe) {
        long activeUsers = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNull("Active");
        BigDecimal totalRev = invoiceRepository.sumPaidAmountSince(LocalDateTime.now().minusYears(1));
        if (totalRev == null) totalRev = BigDecimal.ZERO;

        ReportStatsResponse.KpiCards kpis = ReportStatsResponse.KpiCards.builder()
                .totalRevenue("$" + String.format("%,.2f", totalRev.doubleValue()))
                .revenueTrend("+15.2%")
                .activeUsers(String.format("%,d", activeUsers))
                .activeUsersTrend("+5.4%")
                .systemActivity("98.5%")
                .activityTrend("+0.2%")
                .conversionRate("4.8%")
                .conversionTrend("+1.1%")
                .build();

        // Build Revenue Chart (Simulated for visualization)
        List<ReportStatsResponse.RevenueChart> revChart = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String name = month.format(DateTimeFormatter.ofPattern("MMM"));
            double rev = 2000 + (Math.random() * 5000);
            revChart.add(ReportStatsResponse.RevenueChart.builder()
                    .name(name)
                    .revenue(rev)
                    .expenses(rev * 0.6)
                    .build());
        }

        // Build User Growth Chart (Simulated for visualization)
        List<ReportStatsResponse.UserGrowthChart> userChart = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = now.minusDays(i);
            String name = day.format(DateTimeFormatter.ofPattern("EEE"));
            long act = activeUsers > 0 ? activeUsers - (long)(Math.random() * 50) : (long)(Math.random() * 100);
            long newU = (long)(Math.random() * 20);
            userChart.add(ReportStatsResponse.UserGrowthChart.builder()
                    .name(name)
                    .active(act > 0 ? act : 0)
                    .newUsers(newU)
                    .build());
        }

        return ReportStatsResponse.builder()
                .kpis(kpis)
                .revenueChart(revChart)
                .userGrowthChart(userChart)
                .build();
    }
}
