package com.modsensoftware.marketplace.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

/**
 * @author andrey.demyanchik on 12/8/2022
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    private String username;
    @Pattern(regexp = "(\\w+)@(\\w+\\.)(\\w+)(\\.\\w+)*", message = "Email must be valid.")
    private String email;
    private String name;
    @Length(min = 8, message = "Password must be at least 8 characters long")
    private String password;
    private Long companyId;
}
