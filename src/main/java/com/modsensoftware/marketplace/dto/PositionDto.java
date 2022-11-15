package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionDto {
    private UUID itemId;
    private Long companyId;
    private UUID createdBy;
    private LocalDateTime created;
    // Positive value. Values start at 0.01
    private Double amount;
}
