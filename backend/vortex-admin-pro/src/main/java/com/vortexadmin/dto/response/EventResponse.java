package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private List<AttendeeInfo> attendees;

    @Data
    @Builder
    public static class AttendeeInfo {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String email;
    }
}
