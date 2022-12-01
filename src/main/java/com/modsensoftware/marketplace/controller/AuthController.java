package com.modsensoftware.marketplace.controller;

import com.modsensoftware.marketplace.dto.request.JwtRequest;
import com.modsensoftware.marketplace.dto.request.RefreshJwtRequest;
import com.modsensoftware.marketplace.dto.response.JwtResponse;
import com.modsensoftware.marketplace.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author andrey.demyanchik on 11/30/2022
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody JwtRequest authRequest) {
        return authService.login(authRequest);
    }

    @PostMapping("/token")
    public JwtResponse getNewAccessToken(@Valid @RequestBody RefreshJwtRequest request) {
        return authService.getAccessToken(request.getRefreshToken());
    }

    @PostMapping("/refresh")
    public JwtResponse getNewRefreshToken(@Valid @RequestBody RefreshJwtRequest request) {
        return authService.refresh(request.getRefreshToken());
    }
}
