package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.UUID;

import static com.modsensoftware.marketplace.constants.Constants.INVALID_AMOUNT_MESSAGE;
import static com.modsensoftware.marketplace.constants.Constants.INVALID_MIN_AMOUNT_MESSAGE;
import static com.modsensoftware.marketplace.constants.Constants.MIN_AMOUNT_VALUE;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PositionDto {
    private UUID itemId;
    private Long itemVersion;
    private Long companyId;
    private UUID createdBy;

    @DecimalMin(value = MIN_AMOUNT_VALUE, message = INVALID_AMOUNT_MESSAGE)
    private BigDecimal amount;

    @DecimalMin(value = MIN_AMOUNT_VALUE, message = INVALID_MIN_AMOUNT_MESSAGE)
    private BigDecimal minAmount;
    private Long version;
}
