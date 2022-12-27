package com.modsensoftware.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 12/26/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDto {
    private UUID id;
    private String username;
    private String email;
    private String name;
    private LocalDateTime created;
    private LocalDateTime updated;
    private CompanyResponseDto company;
}
