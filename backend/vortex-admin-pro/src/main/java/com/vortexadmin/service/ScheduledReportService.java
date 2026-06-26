package com.vortexadmin.service;

import com.vortexadmin.dto.response.ReportStatsResponse;
import com.vortexadmin.repository.AuditLogRepository;
import com.vortexadmin.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledReportService {

    private final ReportStatsService reportStatsService;
    private final ReportExportService reportExportService;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${vortex.app.mailFrom:no-reply@vortexadmin.com}")
    private String mailFrom;

    @Value("${vortex.report.email:diwooo1661@gmail.com}")
    private String reportEmail;

    // Every Sunday at 08:00
    @Scheduled(cron = "0 0 8 ? * SUN")
    public void sendWeeklyReport() {
        log.info("Sending weekly report to {}", reportEmail);
        sendReport("Weekly", "7D");
    }

    // Every 1st of month at 08:00
    @Scheduled(cron = "0 0 8 1 * ?")
    public void sendMonthlyReport() {
        log.info("Sending monthly report to {}", reportEmail);
        sendReport("Monthly", "1M");
    }

    private void sendReport(String type, String timeframe) {
        try {
            ReportStatsResponse stats = reportStatsService.getReportStats(timeframe);
            long totalUsers  = userRepository.count();
            long activeUsers = userRepository.countByStatusIgnoreCaseAndDeletedAtIsNull("Active");
            long auditCount  = auditLogRepository.count();
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String html = buildHtml(type, date, totalUsers, activeUsers, auditCount, stats);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(reportEmail);
            helper.setSubject("[Vortex Admin] " + type + " Report — " + date);
            helper.setText(html, true);

            mailSender.send(msg);
            log.info("{} report sent successfully", type);
        } catch (Exception e) {
            log.error("Failed to send {} report: {}", type, e.getMessage(), e);
        }
    }

    private String buildHtml(String type, String date, long total, long active, long audit,
                              ReportStatsResponse stats) {
        String kpiRevenue = stats.getKpis() != null ? stats.getKpis().getTotalRevenue() : "N/A";
        String kpiActive  = stats.getKpis() != null ? stats.getKpis().getActiveUsers()  : String.valueOf(active);

        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family:sans-serif;background:#0f0f13;color:#e4e4e7;margin:0;padding:0;">
              <div style="max-width:600px;margin:32px auto;background:#18181b;border-radius:16px;overflow:hidden;border:1px solid #27272a;">
                <div style="background:linear-gradient(135deg,#4f46e5,#7c3aed);padding:32px;text-align:center;">
                  <h1 style="margin:0;color:#fff;font-size:24px;">Vortex Admin Pro</h1>
                  <p style="margin:8px 0 0;color:rgba(255,255,255,.8);font-size:14px;">%s Report &mdash; %s</p>
                </div>
                <div style="padding:32px;">
                  <div style="display:flex;gap:16px;margin-bottom:24px;">
                    %s %s %s
                  </div>
                  <p style="color:#71717a;font-size:12px;text-align:center;margin-top:32px;">
                    This is an automated report from Vortex Admin Pro. Do not reply to this email.
                  </p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                type, date,
                kpiCard("Total Users", String.valueOf(total), "#4f46e5"),
                kpiCard("Active Users", kpiActive, "#16a34a"),
                kpiCard("Revenue", kpiRevenue, "#d97706")
        );
    }

    private String kpiCard(String label, String value, String color) {
        return """
            <div style="flex:1;background:#27272a;border-radius:12px;padding:20px;text-align:center;border-left:4px solid %s;">
              <div style="font-size:24px;font-weight:700;color:%s;">%s</div>
              <div style="font-size:12px;color:#a1a1aa;margin-top:4px;">%s</div>
            </div>
            """.formatted(color, color, value, label);
    }
}
