package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRequestDto {
    private String name;
    @Pattern(regexp = "(\\w+)@(\\w+\\.)(\\w+)(\\.\\w+)*",
            message = "Email must be valid.")
    private String email;
    private String description;
}
