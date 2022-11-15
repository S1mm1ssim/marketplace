package com.modsensoftware.marketplace.domain;

import com.modsensoftware.marketplace.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String username;
    private String email;
    private String name;
    private Role role;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Company company;
}
