package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskCommentRequest {
    @NotBlank(message = "Comment is required")
    private String comment;
}
