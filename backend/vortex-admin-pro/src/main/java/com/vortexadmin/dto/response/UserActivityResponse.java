package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserActivityResponse {
    private List<ActivityItem> timeline;
    private List<SessionItem> sessions;

    @Data
    @Builder
    public static class ActivityItem {
        private Long id;
        private String action;
        private String entityType;
        private String details;
        private String ipAddress;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class SessionItem {
        private Long id;
        private String ipAddress;
        private String country;
        private String countryCode;
        private String userAgent;
        private LocalDateTime loginAt;
        private LocalDateTime logoutAt;
    }
}
