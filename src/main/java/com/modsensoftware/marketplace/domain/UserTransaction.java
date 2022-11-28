package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 11/24/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_transaction")
@NamedEntityGraph(
        name = "graph.UserTransaction.orders.position",
        attributeNodes = @NamedAttributeNode(value = "orderLine", subgraph = "subgraph.order"),
        subgraphs = {
                @NamedSubgraph(
                        name = "subgraph.order",
                        attributeNodes = @NamedAttributeNode(value = "position", subgraph = "subgraph.position")

                ),
                @NamedSubgraph(
                        name = "subgraph.position",
                        attributeNodes = {
                                @NamedAttributeNode("company"),
                                @NamedAttributeNode(value = "createdBy", subgraph = "subgraph.user"),
                                @NamedAttributeNode(value = "item", subgraph = "subgraph.category")
                        }
                ),
                @NamedSubgraph(
                        name = "subgraph.user",
                        attributeNodes = @NamedAttributeNode(value = "company")
                ),
                @NamedSubgraph(
                        name = "subgraph.category",
                        attributeNodes = @NamedAttributeNode(value = "category")
                )
        }
)
public class UserTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Type(type = "pg-uuid")
    private UUID userId;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @OneToMany(mappedBy = "userTransaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orderLine;
}
