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
import javax.persistence.Version;
import java.time.LocalDateTime;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(
        name = "graph.Position.item.company.user",
        attributeNodes = {
                @NamedAttributeNode(value = "createdBy", subgraph = "subgraph.user.company"),
                @NamedAttributeNode(value = "company"),
                @NamedAttributeNode(value = "item", subgraph = "subgraph.item.category")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "subgraph.item.category",
                        attributeNodes = {
                                @NamedAttributeNode("category")
                        }
                ),
                @NamedSubgraph(
                        name = "subgraph.user.company",
                        attributeNodes = {
                                @NamedAttributeNode("company")
                        }
                )
        }
)
@Entity
@Table(name = "position")
@Builder
public class Position {

    public static final String ID_FIELD_NAME = "id";
    public static final String COMPANY_FIELD_NAME = "company";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    // Positive value. Values start at 0.01
    @Column(name = "amount", nullable = false)
    private Double amount;

    // Positive value. Values start at 0.01
    @Column(name = "min_amount", nullable = false)
    private Double minAmount;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
