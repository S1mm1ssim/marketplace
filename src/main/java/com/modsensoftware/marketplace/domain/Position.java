package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    private Long id;
    private Item item;
    private Company company;
    private User createdBy;
    private LocalDateTime created;
    // Positive value. Values start at 0.01
    private Double amount;
}
