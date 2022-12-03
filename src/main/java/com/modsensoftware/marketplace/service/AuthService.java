package com.modsensoftware.marketplace.service;

import com.modsensoftware.marketplace.dto.request.JwtRequest;
import com.modsensoftware.marketplace.dto.response.JwtResponse;
import lombok.NonNull;

/**
 * @author andrey.demyanchik on 12/1/2022
 */
public interface AuthService {

    JwtResponse login(@NonNull JwtRequest authRequest);

    JwtResponse refresh(@NonNull String refreshToken);
}
