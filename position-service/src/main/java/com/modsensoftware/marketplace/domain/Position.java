package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NamedEntityGraph(
        name = "graph.Position.item.user",
        attributeNodes = {
                @NamedAttributeNode(value = "item", subgraph = "subgraph.item.category")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "subgraph.item.category",
                        attributeNodes = {
                                @NamedAttributeNode("category")
                        }
                )
        }
)
@Entity
@Table(name = "position")
public class Position {

    public static final String ID_FIELD_NAME = "id";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    // Positive value. Values start at 0.01
    @Column(name = "amount", nullable = false)
    private Double amount;

    // Positive value. Values start at 0.01
    @Column(name = "min_amount", nullable = false)
    private Double minAmount;
}
