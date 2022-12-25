package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author andrey.demyanchik on 12/25/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponseDto {

    private Long id;
    private String name;
    private String email;
    private LocalDateTime created;
    private String description;
}
