package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.DashboardDataResponse;
import com.vortexadmin.entity.AuditLog;
import com.vortexadmin.entity.Task;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.UserSession;
import com.vortexadmin.repository.*;
import com.vortexadmin.service.DashboardService;
import com.vortexadmin.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private DataSource dataSource;

    @Override
    @Cacheable(value = "dashboardStats", key = "T(com.vortexadmin.util.SecurityUtils).getCurrentUserId()")
    public DashboardDataResponse getDashboardStats() {
        User currentUser = userRepository.findById(SecurityUtils.getCurrentUserId()).orElseThrow();

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDateTime currentStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime currentEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime previousStart = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime previousEnd = previousMonth.atEndOfMonth().atTime(23, 59, 59);

        // 1. Stat Cards — all counts computed in the database
        DashboardDataResponse.StatCards statCards = DashboardDataResponse.StatCards.builder()
                .totalUsers(userRepository.countByDeletedAtIsNull())
                .totalUsersTrend(calculateTrend(
                        userRepository.countByDeletedAtIsNullAndCreatedAtBetween(currentStart, currentEnd),
                        userRepository.countByDeletedAtIsNullAndCreatedAtBetween(previousStart, previousEnd)))
                .activeUsers(userRepository.countByStatusIgnoreCaseAndDeletedAtIsNull("Active"))
                .activeUsersTrend(calculateTrend(
                        userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween("Active", currentStart, currentEnd),
                        userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween("Active", previousStart, previousEnd)))
                .totalTeams(teamRepository.count())
                .totalTeamsTrend(calculateTrend(
                        teamRepository.countByCreatedAtBetween(currentStart, currentEnd),
                        teamRepository.countByCreatedAtBetween(previousStart, previousEnd)))
                .totalTasks(taskRepository.count())
                .totalTasksTrend(calculateTrend(
                        taskRepository.countByCreatedAtBetween(currentStart, currentEnd),
                        taskRepository.countByCreatedAtBetween(previousStart, previousEnd)))
                .totalEvents(eventRepository.count())
                .totalEventsTrend(calculateTrend(
                        eventRepository.countByCreatedAtBetween(currentStart, currentEnd),
                        eventRepository.countByCreatedAtBetween(previousStart, previousEnd)))
                .unreadNotifications(notificationRepository.countByUserAndIsReadFalse(currentUser))
                .unreadNotificationsTrend(calculateTrend(
                        notificationRepository.countByUserAndCreatedAtBetween(currentUser, currentStart, currentEnd),
                        notificationRepository.countByUserAndCreatedAtBetween(currentUser, previousStart, previousEnd)))
                .build();

        return DashboardDataResponse.builder()
                .statCards(statCards)
                .userGrowthChart(generateUserGrowthChart())
                .taskActivityChart(generateTaskActivityChart())
                .loginActivityChart(generateLoginActivityChart())
                .roleDistribution(generateRoleDistribution())
                .systemHealth(generateSystemHealth())
                .recentActivities(generateRecentActivities())
                .latestUsers(generateLatestUsers())
                .build();
    }

    private String calculateTrend(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }
        long percent = Math.round(((current - previous) * 100.0) / previous);
        return percent > 0 ? "+" + percent + "%" : percent + "%";
    }

    private List<DashboardDataResponse.ChartData> generateUserGrowthChart() {
        LocalDate now = LocalDate.now();
        List<DashboardDataResponse.ChartData> chart = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String monthName = monthDate.getMonth().name().substring(0, 3);
            LocalDateTime monthEnd = YearMonth.from(monthDate).atEndOfMonth().atTime(23, 59, 59);

            chart.add(DashboardDataResponse.ChartData.builder()
                    .name(monthName)
                    .users(userRepository.countByDeletedAtIsNullAndCreatedAtLessThanEqual(monthEnd))
                    .active(userRepository.countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtLessThanEqual("Active", monthEnd))
                    .build());
        }
        return chart;
    }

    private List<DashboardDataResponse.ChartData> generateTaskActivityChart() {
        Map<String, long[]> taskCountsByDay = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE"); // Mon, Tue...

        for (int i = 6; i >= 0; i--) {
            String dayName = now.minusDays(i).format(formatter);
            taskCountsByDay.put(dayName, new long[]{0L, 0L}); // [created, completed]
        }

        List<Task> recentTasks = taskRepository.findByCreatedAtAfter(now.minusDays(7).atStartOfDay());
        for (Task t : recentTasks) {
            if (t.getCreatedAt() != null) {
                LocalDate created = t.getCreatedAt().toLocalDate();
                if (created.isAfter(now.minusDays(7))) {
                    String dayName = created.format(formatter);
                    if (taskCountsByDay.containsKey(dayName)) {
                        taskCountsByDay.get(dayName)[0]++;
                        if ("DONE".equalsIgnoreCase(t.getStatus())) {
                            taskCountsByDay.get(dayName)[1]++;
                        }
                    }
                }
            }
        }

        List<DashboardDataResponse.ChartData> chart = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : taskCountsByDay.entrySet()) {
            chart.add(DashboardDataResponse.ChartData.builder()
                    .name(entry.getKey())
                    .created(entry.getValue()[0])
                    .completed(entry.getValue()[1])
                    .build());
        }
        return chart;
    }

    private List<DashboardDataResponse.ChartData> generateLoginActivityChart() {
        Map<String, long[]> loginCountsByMonth = new LinkedHashMap<>(); // [totalLogins, activeSessions]
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            String monthName = now.minusMonths(i).getMonth().name().substring(0, 3);
            loginCountsByMonth.put(monthName, new long[]{0L, 0L});
        }

        List<UserSession> sessions = userSessionRepository.findByLoginAtAfter(now.minusMonths(6).atStartOfDay());
        for (UserSession session : sessions) {
            if (session.getLoginAt() != null) {
                String monthName = session.getLoginAt().getMonth().name().substring(0, 3);
                long[] counts = loginCountsByMonth.get(monthName);
                if (counts != null) {
                    counts[0]++;
                    if (session.getLogoutAt() == null) {
                        counts[1]++;
                    }
                }
            }
        }

        List<DashboardDataResponse.ChartData> chart = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : loginCountsByMonth.entrySet()) {
            chart.add(DashboardDataResponse.ChartData.builder()
                    .name(entry.getKey())
                    .users(entry.getValue()[0])
                    .active(entry.getValue()[1])
                    .build());
        }
        return chart;
    }

    private List<DashboardDataResponse.DistributionData> generateRoleDistribution() {
        List<DashboardDataResponse.DistributionData> distribution = new ArrayList<>();
        for (Object[] row : userRepository.countUsersByRole()) {
            distribution.add(DashboardDataResponse.DistributionData.builder()
                    .name((String) row[0])
                    .value((Long) row[1])
                    .build());
        }
        return distribution;
    }

    private DashboardDataResponse.SystemHealth generateSystemHealth() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;

        long memoryUsagePct = maxMemory > 0 ? (usedMemory * 100) / maxMemory : 0;

        File root = new File("/");
        long totalSpace = root.getTotalSpace();
        long freeSpace = root.getFreeSpace();
        long usedSpace = totalSpace - freeSpace;
        long storageUsagePct = totalSpace > 0 ? (usedSpace * 100) / totalSpace : 0;

        long cpuUsagePct = 0;
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getCpuLoad();
        if (cpuLoad >= 0) {
            cpuUsagePct = Math.round(cpuLoad * 100);
        }

        String databaseStatus;
        try (Connection connection = dataSource.getConnection()) {
            databaseStatus = connection.isValid(2) ? "Connected" : "Disconnected";
        } catch (SQLException e) {
            databaseStatus = "Disconnected";
        }

        return DashboardDataResponse.SystemHealth.builder()
                .cpuUsage(cpuUsagePct + "%")
                .memoryUsage(memoryUsagePct + "%")
                .storageUsage(storageUsagePct + "%")
                .databaseStatus(databaseStatus)
                .build();
    }

    private List<DashboardDataResponse.ActivityDto> generateRecentActivities() {
        List<DashboardDataResponse.ActivityDto> activities = new ArrayList<>();
        for (AuditLog log : auditLogRepository.findTop5ByOrderByCreatedAtDesc()) {
            String type = "primary";
            if ("DELETE".equalsIgnoreCase(log.getAction())) type = "danger";
            else if ("CREATE".equalsIgnoreCase(log.getAction())) type = "success";
            else if ("UPDATE".equalsIgnoreCase(log.getAction())) type = "warning";

            String timeAgo = "Just now";
            if (log.getCreatedAt() != null) {
                long minutes = ChronoUnit.MINUTES.between(log.getCreatedAt(), LocalDateTime.now());
                if (minutes > 60 * 24) timeAgo = (minutes / (60 * 24)) + " days ago";
                else if (minutes > 60) timeAgo = (minutes / 60) + " hours ago";
                else if (minutes > 0) timeAgo = minutes + " mins ago";
            }

            activities.add(DashboardDataResponse.ActivityDto.builder()
                    .title(log.getAction() + " " + log.getEntityType())
                    .desc(log.getDetails())
                    .time(timeAgo)
                    .type(type)
                    .build());
        }
        return activities;
    }

    private List<DashboardDataResponse.UserDto> generateLatestUsers() {
        List<DashboardDataResponse.UserDto> latest = new ArrayList<>();
        for (User u : userRepository.findTop4ByDeletedAtIsNullOrderByCreatedAtDesc()) {
            latest.add(DashboardDataResponse.UserDto.builder()
                    .id(u.getId().toString())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .status(u.getStatus())
                    .avatarText((u.getFirstName() != null ? u.getFirstName().substring(0, 1) : u.getUsername().substring(0, 1)).toUpperCase())
                    .build());
        }
        return latest;
    }
}
