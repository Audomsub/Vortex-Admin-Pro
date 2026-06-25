package com.vortexadmin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountEligibilityResponse {
    private boolean loyaltyDiscountEligible;
    private boolean firstYearDiscountEligible;
}
