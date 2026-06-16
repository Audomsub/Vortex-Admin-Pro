package com.vortexadmin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpgradePlanRequest {

    @NotNull(message = "Organization id is required")
    private Long organizationId;

    @NotBlank(message = "Plan name is required")
    private String planName;

    @Pattern(regexp = "MONTHLY|YEARLY", message = "Billing cycle must be MONTHLY or YEARLY")
    private String billingCycle = "MONTHLY";
}
