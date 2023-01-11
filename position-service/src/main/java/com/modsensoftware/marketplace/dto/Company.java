package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Company {

    private Long id;
    private String name;
    private String email;
    private LocalDateTime created;
    private String description;
}
