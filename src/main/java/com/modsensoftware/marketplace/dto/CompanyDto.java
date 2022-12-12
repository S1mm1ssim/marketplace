package com.modsensoftware.marketplace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

import static com.modsensoftware.marketplace.constants.Constants.EMAIL_REGEX;
import static com.modsensoftware.marketplace.constants.Constants.INVALID_EMAIL_MESSAGE;

/**
 * @author andrey.demyanchik on 11/3/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDto {
    private String name;
    @Email(regexp = EMAIL_REGEX, message = INVALID_EMAIL_MESSAGE)
    private String email;
    private String description;
}
