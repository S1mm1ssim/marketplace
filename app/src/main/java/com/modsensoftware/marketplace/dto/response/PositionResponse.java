package com.modsensoftware.marketplace.dto.response;

import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.Company;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author andrey.demyanchik on 12/19/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionResponse {
    private Long id;
    private Item item;
    private Company company;
    private UserResponse createdBy;
    private LocalDateTime created;
    private Double amount;
    private Double minAmount;
    private Long version;
}
