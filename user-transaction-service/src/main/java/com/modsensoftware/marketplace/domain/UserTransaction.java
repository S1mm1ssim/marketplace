package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
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
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
@Entity
@Table(name = "user_transaction", schema = "user_transaction_service")
@NamedEntityGraph(
        name = "graph.UserTransaction.orders.position",
        attributeNodes = @NamedAttributeNode(value = "orderLine")
)
public class UserTransaction {

    public static final String USER_ID_FIELD_NAME = "userId";
    public static final String CREATED_FIELD_NAME = "created";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Type(type = "pg-uuid")
    private UUID userId;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Type(type = "pgsql_enum")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "transaction_status")
    private UserTransactionStatus status;

    @OneToMany(mappedBy = "userTransaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orderLine;
}
