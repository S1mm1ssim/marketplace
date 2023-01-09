package com.modsensoftware.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePositionRequestDto {
    private String itemId;
    private Long itemVersion;

    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @DecimalMin(value = "0.01")
    private BigDecimal minAmount;
}
