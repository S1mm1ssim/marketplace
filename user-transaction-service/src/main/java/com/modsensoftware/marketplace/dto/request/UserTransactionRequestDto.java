package com.modsensoftware.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/24/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTransactionRequestDto {

    private UUID userId;
    private List<OrderRequestDto> orderLine;
}
