package com.modsensoftware.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 12/27/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionRequestDto {
    private UUID itemId;
    private Long itemVersion;
    private Long companyId;
    private UUID createdBy;

    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @DecimalMin(value = "0.01")
    private BigDecimal minAmount;
    private Long version;
}
