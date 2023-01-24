package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private String name;
    private String description;
    private String categoryId;
    private Long version;
}
