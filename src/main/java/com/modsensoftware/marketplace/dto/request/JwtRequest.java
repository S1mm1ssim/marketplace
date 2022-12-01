package com.modsensoftware.marketplace.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author andrey.demyanchik on 12/1/2022
 */
@Setter
@Getter
public class JwtRequest {

    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
