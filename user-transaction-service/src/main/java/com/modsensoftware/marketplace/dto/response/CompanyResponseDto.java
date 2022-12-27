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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime created;
    private String description;
}
