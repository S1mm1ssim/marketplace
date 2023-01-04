package com.modsensoftware.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private CategoryResponse parent;
}
