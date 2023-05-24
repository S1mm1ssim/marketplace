package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "items")
public class Item {

    @MongoId
    private String id;
    private String name;
    private String description;
    @CreatedDate
    private LocalDateTime created;
    private Category category;
    @Version
    private Long version;
}
