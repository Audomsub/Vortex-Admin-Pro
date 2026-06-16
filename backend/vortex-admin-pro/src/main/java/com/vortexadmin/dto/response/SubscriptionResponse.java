package com.vortexadmin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private PlanResponse plan;
    private String status;
    private String billingCycle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Usage tracking
    private long currentUsers;
    private Integer maxUsers;
    private long storageUsedMb;
    private Long maxStorageMb;
}
