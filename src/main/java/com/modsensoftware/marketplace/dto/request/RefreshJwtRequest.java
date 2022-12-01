package com.modsensoftware.marketplace.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author andrey.demyanchik on 12/1/2022
 */
@Getter
@Setter
public class RefreshJwtRequest {

    @NotBlank
    private String refreshToken;
}
