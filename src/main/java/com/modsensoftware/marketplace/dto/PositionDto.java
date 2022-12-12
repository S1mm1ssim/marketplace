package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PositionDto {
    private UUID itemId;
    private Long companyId;
    private UUID createdBy;
    // Positive value. Values start at 0.01
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    private Long version;
}
