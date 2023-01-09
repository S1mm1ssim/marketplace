package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "categories")
public class Category {

    @MongoId
    private String id;
    private String name;
    private String description;
    private Category parent;
}
