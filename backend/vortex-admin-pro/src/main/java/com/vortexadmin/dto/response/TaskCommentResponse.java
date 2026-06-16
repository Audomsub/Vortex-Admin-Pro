package com.vortexadmin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskCommentResponse {
    private Long id;
    private Long taskId;
    private Long userId;
    private String username;
    private String avatarUrl;
    private String comment;
    private LocalDateTime createdAt;
}
