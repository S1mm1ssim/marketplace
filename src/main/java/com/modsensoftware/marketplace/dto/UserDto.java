package com.modsensoftware.marketplace.dto;

import com.modsensoftware.marketplace.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String username;
    private String email;
    private String name;
    private Role role;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Long companyId;
}
