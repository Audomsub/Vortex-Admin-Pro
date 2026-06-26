package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkActionRequest {

    @NotEmpty(message = "User IDs must not be empty")
    private List<Long> userIds;

    @NotNull(message = "Action is required")
    private String action; // SUSPEND, ACTIVATE, DELETE, CHANGE_ROLE

    private Long roleId;
}
