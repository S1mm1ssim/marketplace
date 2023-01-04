package com.modsensoftware.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 12/26/2022
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime created;
    private CategoryResponse category;
    private Long version;
}
