package com.modsensoftware.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;

import static com.modsensoftware.marketplace.constants.Constants.EMAIL_REGEX;
import static com.modsensoftware.marketplace.constants.Constants.INVALID_EMAIL_MESSAGE;
import static com.modsensoftware.marketplace.constants.Constants.INVALID_PASSWORD_LENGTH_MESSAGE;

/**
 * @author andrey.demyanchik on 12/8/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String username;
    @Email(regexp = EMAIL_REGEX, message = INVALID_EMAIL_MESSAGE)
    private String email;
    private String name;
    @Length(min = 8, message = INVALID_PASSWORD_LENGTH_MESSAGE)
    private String password;
    private Long companyId;
}
