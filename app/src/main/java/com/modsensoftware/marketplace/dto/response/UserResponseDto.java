package com.modsensoftware.marketplace.dto.response;

import com.modsensoftware.marketplace.dto.CompanyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 12/8/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String username;
    private String email;
    private String name;
    private LocalDateTime created;
    private LocalDateTime updated;
    private CompanyDto company;
}
