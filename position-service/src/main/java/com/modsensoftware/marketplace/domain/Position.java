package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
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
@Document(collection = "positions")
public class Position {

    public static final String ID_FIELD_NAME = "id";

    @MongoId
    private String id;
    private Item item;
    private Long companyId;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private LocalDateTime created;

    // Positive value. Values start at 0.01
    private Double amount;

    // Positive value. Values start at 0.01
    private Double minAmount;
}
