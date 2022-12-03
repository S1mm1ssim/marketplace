package com.modsensoftware.marketplace.dto.response;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author andrey.demyanchik on 12/1/2022
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class JwtResponse {

    private final String type = "Bearer";
    private String accessToken;
    private String refreshToken;

}
