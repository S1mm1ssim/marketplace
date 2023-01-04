package com.modsensoftware.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

import static com.modsensoftware.marketplace.constants.Constants.INVALID_AMOUNT_MESSAGE;
import static com.modsensoftware.marketplace.constants.Constants.MIN_AMOUNT_VALUE;

/**
 * @author andrey.demyanchik on 11/24/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private Long positionId;

    @DecimalMin(value = MIN_AMOUNT_VALUE, message = INVALID_AMOUNT_MESSAGE)
    private BigDecimal amount;

    private Long positionVersion;
}
