package com.modsensoftware.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author andrey.demyanchik on 12/26/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PositionResponseDto {
    private Long id;
    private ItemResponseDto item;
    private CompanyResponseDto company;
    private UserResponseDto createdBy;
    private LocalDateTime created;
    private Double amount;
    private Double minAmount;
}
