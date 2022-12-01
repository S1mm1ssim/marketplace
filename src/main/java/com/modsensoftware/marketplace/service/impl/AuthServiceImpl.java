package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.jwt.JwtProvider;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.request.JwtRequest;
import com.modsensoftware.marketplace.dto.response.JwtResponse;
import com.modsensoftware.marketplace.exception.AuthorizationException;
import com.modsensoftware.marketplace.service.AuthService;
import com.modsensoftware.marketplace.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andrey.demyanchik on 12/1/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${exception.message.incorrectCredentialsException}")
    private String incorrectCredentialsExceptionMessage;
    @Value("${exception.message.invalidJwt}")
    private String invalidJwtMessage;

    @Override
    public JwtResponse login(@NonNull JwtRequest authRequest) {
        log.debug("Logging in user. Auth request: {}", authRequest);
        final User user = userService.getUserByEmail(authRequest.getEmail());
        if (bCryptPasswordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String refreshToken = jwtProvider.generateRefreshToken(user);
            refreshStorage.put(user.getEmail(), refreshToken);
            log.info("Logged in successfully");
            return new JwtResponse(accessToken, refreshToken);
        } else {
            log.error("Could not login user due to invalid credentials");
            throw new AuthorizationException(incorrectCredentialsExceptionMessage);
        }
    }

    @Override
    public JwtResponse getAccessToken(@NonNull String refreshToken) {
        log.debug("Validating refresh token: {}", refreshToken);
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            log.debug("Validation successful");
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                log.debug("Token present in storage");
                final User user = userService.getUserByEmail(login);
                final String accessToken = jwtProvider.generateAccessToken(user);
                log.debug("Sending new access token");
                return new JwtResponse(accessToken, null);
            }
        }
        log.debug("Validation failed");
        throw new AuthorizationException(invalidJwtMessage);
    }

    @Override
    public JwtResponse refresh(@NonNull String refreshToken) {
        log.debug("Validating refresh token: {}", refreshToken);
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            log.debug("Validation successful");
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(login);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                log.debug("Token present in storage");
                final User user = userService.getUserByEmail(login);
                final String accessToken = jwtProvider.generateAccessToken(user);
                // Can remove code duplication
                final String newRefreshToken = jwtProvider.generateRefreshToken(user);
                refreshStorage.put(user.getEmail(), newRefreshToken);
                return new JwtResponse(accessToken, newRefreshToken);
            }
        }
        log.error("Validation failed");
        throw new AuthorizationException(invalidJwtMessage);
    }
}
