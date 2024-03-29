package com.modsensoftware.marketplace.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Builder
@Table(name = "\"user\"", schema = "user_service")
public class User {

    public static final String ID_FIELD_NAME = "id";
    public static final String USERNAME_FIELD_NAME = "username";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String FULL_NAME_FIELD_NAME = "name";
    public static final String CREATED_FIELD_NAME = "created";
    public static final String UPDATED_FIELD_NAME = "updated";
    public static final String COMPANY_ID_FIELD_NAME = "companyId";

    @Id
    @Type(type = "pg-uuid")
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @Column(name = "company_id", nullable = false)
    private Long companyId;
}
