package com.vortexadmin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private Long id;
    private String name;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer maxUsers;
    private Long maxStorageMb;
}
