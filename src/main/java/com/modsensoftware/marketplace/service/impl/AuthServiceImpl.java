package com.modsensoftware.marketplace.service.impl;

import com.modsensoftware.marketplace.config.jwt.JwtProvider;
import com.modsensoftware.marketplace.domain.RefreshToken;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.request.JwtRequest;
import com.modsensoftware.marketplace.dto.response.JwtResponse;
import com.modsensoftware.marketplace.exception.AuthorizationException;
import com.modsensoftware.marketplace.repository.RefreshTokenRepository;
import com.modsensoftware.marketplace.service.AuthService;
import com.modsensoftware.marketplace.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author andrey.demyanchik on 12/1/2022
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${exception.message.invalidCredentialsException}")
    private String invalidCredentialsExceptionMessage;
    @Value("${exception.message.invalidJwt}")
    private String invalidJwtMessage;

    private static final int MAX_TOKENS_SAVED_FOR_USER = 1;
    private static final int FIRST_TOKEN_ID_IN_LIST = 0;

    @Override
    public JwtResponse login(@NonNull JwtRequest authRequest) {
        log.debug("Logging in user with email {}", authRequest.getEmail());
        final User user = userService.getUserByEmail(authRequest.getEmail());
        if (bCryptPasswordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String refreshToken = jwtProvider.generateRefreshToken(user);
            List<RefreshToken> userTokens = refreshTokenRepository.findByUserEmail(user.getEmail());
            if (userTokens.size() == MAX_TOKENS_SAVED_FOR_USER) {
                log.debug("User already had a refresh token. Deleting previous one");
                refreshTokenRepository.deleteAll(userTokens);
            }
            refreshTokenRepository.save(new RefreshToken(user.getEmail(), refreshToken));
            log.info("Logged in successfully");
            return new JwtResponse(accessToken, refreshToken);
        } else {
            log.error("Could not login user due to invalid credentials");
            throw new AuthorizationException(invalidCredentialsExceptionMessage);
        }
    }

    @Override
    public JwtResponse getAccessToken(@NonNull String refreshToken) {
        log.debug("Validating refresh token: {}", refreshToken);
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            log.debug("Token is valid");
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String userEmail = claims.getSubject();
            List<RefreshToken> savedRefreshTokens = refreshTokenRepository.findByUserEmail(userEmail);
            if (savedRefreshTokens.size() == MAX_TOKENS_SAVED_FOR_USER) {
                log.debug("Token present in storage");
                String token = savedRefreshTokens.get(FIRST_TOKEN_ID_IN_LIST).getRefreshToken();
                if (token.equals(refreshToken)) {
                    log.debug("Tokens match");
                    final User user = userService.getUserByEmail(userEmail);
                    final String accessToken = jwtProvider.generateAccessToken(user);
                    log.debug("Sending new access token");
                    return new JwtResponse(accessToken, null);
                } else {
                    log.debug("Tokens don't match");
                }
            }
        }
        log.debug("Validation failed");
        throw new AuthorizationException(invalidJwtMessage);
    }

    @Override
    public JwtResponse refresh(@NonNull String refreshToken) {
        log.debug("Validating refresh token: {}", refreshToken);
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            log.debug("Token is valid");
            final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            final String login = claims.getSubject();
            List<RefreshToken> savedRefreshToken = refreshTokenRepository.findByUserEmail(login);
            if (savedRefreshToken.size() == MAX_TOKENS_SAVED_FOR_USER) {
                log.debug("Token present in storage");
                String token = savedRefreshToken.get(FIRST_TOKEN_ID_IN_LIST).getRefreshToken();
                if (token.equals(refreshToken)) {
                    log.debug("Tokens match");
                    final User user = userService.getUserByEmail(login);
                    final String accessToken = jwtProvider.generateAccessToken(user);
                    final String newRefreshToken = jwtProvider.generateRefreshToken(user);
                    refreshTokenRepository.delete(savedRefreshToken.get(FIRST_TOKEN_ID_IN_LIST));
                    refreshTokenRepository.save(new RefreshToken(user.getEmail(), newRefreshToken));
                    return new JwtResponse(accessToken, newRefreshToken);
                } else {
                    log.debug("Tokens don't match");
                }
            }
        }
        log.error("Validation failed");
        throw new AuthorizationException(invalidJwtMessage);
    }
}
