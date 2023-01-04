package com.modsensoftware.marketplace.dto;

import com.modsensoftware.marketplace.domain.UserTransactionStatus;
import com.modsensoftware.marketplace.dto.request.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author andrey.demyanchik on 12/29/2022
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PlacedUserTransaction {

    private Long id;
    private UserTransactionStatus status;
    private List<OrderRequest> orderLine;
}
