package com.modsensoftware.marketplace.domain;

import com.modsensoftware.marketplace.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"user\"")
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
@NamedEntityGraph(
        name = "graph.User.company",
        attributeNodes = {
                @NamedAttributeNode("company")
        }
)
public class User {

    public static final String ID_FIELD_NAME = "id";
    public static final String USERNAME_FIELD_NAME = "username";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String FULL_NAME_FIELD_NAME = "name";
    public static final String CREATED_FIELD_NAME = "created";
    public static final String UPDATED_FIELD_NAME = "updated";
    public static final String COMPANY_FIELD_NAME = "company";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Type(type = "pg-uuid")
    private UUID id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "user_role", name = "role", nullable = false)
    @Type(type = "pgsql_enum")
    private Role role;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
