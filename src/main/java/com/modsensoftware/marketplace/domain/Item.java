package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

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
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "item")
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "graph.Item.category.parent",
                attributeNodes = {
                        @NamedAttributeNode(value = "category", subgraph = "parent-category-subgraph")
                },
                subgraphs = {
                        @NamedSubgraph(
                                name = "parent-category-subgraph",
                                attributeNodes = {
                                        @NamedAttributeNode("parent")
                                }
                        )
                }
        ), @NamedEntityGraph(
        name = "graph.Item.category",
        attributeNodes = {
                @NamedAttributeNode(value = "category")
        })
})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "pg-uuid")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
