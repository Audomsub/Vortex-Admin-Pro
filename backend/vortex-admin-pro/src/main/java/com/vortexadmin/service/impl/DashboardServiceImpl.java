package com.vortexadmin.service.impl;

import com.vortexadmin.dto.response.DashboardDataResponse;
import com.vortexadmin.entity.AuditLog;
import com.vortexadmin.entity.Task;
import com.vortexadmin.entity.User;
import com.vortexadmin.entity.UserSession;
import com.vortexadmin.repository.*;
import com.vortexadmin.service.DashboardService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import com.vortexadmin.exception.ApiException;
import java.lang.management.ManagementFactory;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Handles dashboard data aggregation business logic, assembling stat cards, chart data,
 * role distribution, real-time system health metrics, recent audit activities, and the
 * latest registered users into a single cached response per authenticated user.
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final TeamRepository teamRepository;
    private final EventRepository eventRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserSessionRepository userSessionRepository;
    private final DataSource dataSource;

    /**
     * Assembles and returns the full dashboard data for the current user.
     * The response includes stat cards (with month-over-month trend percentages),
     * a 6-month user growth chart, a 7-day task activity chart, a 6-month login activity
     * chart, a role distribution breakdown, a live system health snapshot, the 5 most
     * recent audit log entries, and the 4 most recently registered users.
     * Results are cached per user id using the {@code dashboard} cache.
     *
     * @return the fully populated {@link DashboardDataResponse} for the current user
     * @throws ApiException with {@code 404} if the current user record does not exist
     */
    @Cacheable(value = "dashboard", key = "T(com.vortexadmin.util.SecurityUtils).getCurrentUserId()")
    @Override
    public DashboardDataResponse getDashboardStats() {
        User currentUser = userRepository.findById(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

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

    /**
     * Calculates a month-over-month trend percentage string from two counts.
     * Returns "+100%" when there was no previous value but there is a current value,
     * "0%" when the previous count is zero and there is no growth, and the signed
     * percentage otherwise (e.g. "+25%" or "-10%").
     *
     * @param current  the count for the current period
     * @param previous the count for the previous period
     * @return a signed percentage trend string such as "+12%" or "-5%"
     */
    private String calculateTrend(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }
        long percent = Math.round(((current - previous) * 100.0) / previous);
        return percent > 0 ? "+" + percent + "%" : percent + "%";
    }

    /**
     * Generates a 6-month user growth chart by computing the cumulative total and active
     * user counts at the end of each of the last six calendar months.
     *
     * @return a list of six {@link DashboardDataResponse.ChartData} entries, each labelled
     *         with a 3-letter month abbreviation and populated with {@code users} and {@code active} counts
     */
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

    /**
     * Generates a 7-day task activity chart by counting created and completed (DONE) tasks
     * for each of the last seven days. Tasks are fetched in one query and bucketed by day
     * name abbreviation (e.g. "Mon", "Tue").
     *
     * @return a list of seven {@link DashboardDataResponse.ChartData} entries, each labelled
     *         with a 3-letter day abbreviation and populated with {@code created} and {@code completed} counts
     */
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
                if (!created.isBefore(now.minusDays(7))) {
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

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yyyy");

    /**
     * Generates a 6-month login activity chart by counting total logins and concurrent
     * active sessions (sessions with no logout timestamp) per calendar month.
     *
     * @return a list of six {@link DashboardDataResponse.ChartData} entries, each labelled
     *         with "MMM yyyy" and populated with {@code users} (total logins) and
     *         {@code active} (open sessions) counts
     */
    private List<DashboardDataResponse.ChartData> generateLoginActivityChart() {
        Map<YearMonth, long[]> loginCountsByMonth = new LinkedHashMap<>(); // [totalLogins, activeSessions]
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            loginCountsByMonth.put(YearMonth.from(now.minusMonths(i)), new long[]{0L, 0L});
        }

        List<UserSession> sessions = userSessionRepository.findByLoginAtAfter(now.minusMonths(6).atStartOfDay());
        for (UserSession session : sessions) {
            if (session.getLoginAt() != null) {
                YearMonth ym = YearMonth.from(session.getLoginAt());
                long[] counts = loginCountsByMonth.get(ym);
                if (counts != null) {
                    counts[0]++;
                    if (session.getLogoutAt() == null) {
                        counts[1]++;
                    }
                }
            }
        }

        List<DashboardDataResponse.ChartData> chart = new ArrayList<>();
        for (Map.Entry<YearMonth, long[]> entry : loginCountsByMonth.entrySet()) {
            chart.add(DashboardDataResponse.ChartData.builder()
                    .name(entry.getKey().format(MONTH_FMT))
                    .users(entry.getValue()[0])
                    .active(entry.getValue()[1])
                    .build());
        }
        return chart;
    }

    /**
     * Generates the role distribution data by querying the number of users per role name
     * directly from the database.
     *
     * @return a list of {@link DashboardDataResponse.DistributionData} entries, each with
     *         a role name and the count of users assigned to that role
     */
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

    /**
     * Captures real-time JVM memory usage, disk storage usage, CPU load (via the
     * {@code com.sun.management.OperatingSystemMXBean} extension), and database
     * connectivity by acquiring and validating a JDBC connection with a 2-second timeout.
     * CPU is reported as 0% if the JVM does not expose the extended OS bean.
     *
     * @return a {@link DashboardDataResponse.SystemHealth} snapshot with percentage strings
     *         for CPU, memory, and storage, plus a "Connected" or "Disconnected" database status
     */
    private DashboardDataResponse.SystemHealth generateSystemHealth() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = allocatedMemory - freeMemory;

        long memoryUsagePct = maxMemory > 0 ? (usedMemory * 100) / maxMemory : 0;

        File root = Paths.get("").toAbsolutePath().toFile();
        long totalSpace = root.getTotalSpace();
        long freeSpace = root.getFreeSpace();
        long usedSpace = totalSpace - freeSpace;
        long storageUsagePct = totalSpace > 0 ? (usedSpace * 100) / totalSpace : 0;

        long cpuUsagePct = 0;
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = osBean.getCpuLoad();
            if (cpuLoad >= 0) {
                cpuUsagePct = Math.round(cpuLoad * 100);
            }
        } catch (ClassCastException ignored) {
            // Fallback: leave cpuUsagePct at 0 if JVM does not expose this bean
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

    /**
     * Fetches the 5 most recent audit log entries and converts each into an activity DTO
     * with a human-readable relative time string (e.g. "5 mins ago", "2 hours ago") and
     * a color-coded type ("danger" for DELETE, "success" for CREATE, "warning" for UPDATE,
     * "primary" for all others).
     *
     * @return a list of up to 5 {@link DashboardDataResponse.ActivityDto} entries
     */
    private List<DashboardDataResponse.ActivityDto> generateRecentActivities() {
        List<DashboardDataResponse.ActivityDto> activities = new ArrayList<>();
        for (AuditLog log : auditLogRepository.findTop5WithUser(org.springframework.data.domain.PageRequest.of(0, 5))) {
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

    /**
     * Fetches the 4 most recently registered (non-deleted) users and maps each to a
     * lightweight user DTO that includes an avatar text fallback: the first letter of the
     * first name if available, otherwise the first letter of the username, or "?" if both
     * are absent.
     *
     * @return a list of up to 4 {@link DashboardDataResponse.UserDto} entries for the newest users
     */
    private List<DashboardDataResponse.UserDto> generateLatestUsers() {
        List<DashboardDataResponse.UserDto> latest = new ArrayList<>();
        for (User u : userRepository.findTop4ByDeletedAtIsNullOrderByCreatedAtDesc()) {
            latest.add(DashboardDataResponse.UserDto.builder()
                    .id(u.getId().toString())
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .status(u.getStatus())
                    .avatarText(resolveAvatarText(u))
                    .build());
        }
        return latest;
    }

    /**
     * Resolves a single-character avatar text for a user by checking the first name first,
     * then falling back to the username, and finally to "?" if neither is available.
     *
     * @param u the user entity whose avatar text is needed
     * @return a single uppercase character representing the user, or "?" if no name is set
     */
    private String resolveAvatarText(com.vortexadmin.entity.User u) {
        if (u.getFirstName() != null && !u.getFirstName().isEmpty()) {
            return u.getFirstName().substring(0, 1).toUpperCase();
        }
        if (u.getUsername() != null && !u.getUsername().isEmpty()) {
            return u.getUsername().substring(0, 1).toUpperCase();
        }
        return "?";
    }
}
