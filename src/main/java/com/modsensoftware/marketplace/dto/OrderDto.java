package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * @author andrey.demyanchik on 11/24/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long positionId;

    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
