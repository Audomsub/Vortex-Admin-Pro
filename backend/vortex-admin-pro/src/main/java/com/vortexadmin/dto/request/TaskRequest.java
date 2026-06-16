package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;
    
    private String description;
    
    private String status;
    private String priority;
    
    private Long assignedTo;
    private Long teamId;
    
    private LocalDateTime dueDate;
}
