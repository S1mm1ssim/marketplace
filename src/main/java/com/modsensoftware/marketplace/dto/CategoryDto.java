package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private String name;
    private String description;

    // If parent id comes as null from client it does not affect anything
    // Only nonNull values matter
    private Long parentId;

    // If nullParent is true then parentId on entity will be set to null
    private boolean nullParent;
}
