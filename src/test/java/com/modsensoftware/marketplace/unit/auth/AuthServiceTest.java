package com.modsensoftware.marketplace.unit.auth;

import com.modsensoftware.marketplace.config.jwt.JwtProvider;
import com.modsensoftware.marketplace.domain.RefreshToken;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.dto.request.JwtRequest;
import com.modsensoftware.marketplace.dto.response.JwtResponse;
import com.modsensoftware.marketplace.enums.Role;
import com.modsensoftware.marketplace.exception.AuthorizationException;
import com.modsensoftware.marketplace.repository.RefreshTokenRepository;
import com.modsensoftware.marketplace.service.UserService;
import com.modsensoftware.marketplace.service.impl.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author andrey.demyanchik on 12/3/2022
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private AuthServiceImpl underTest;

    private static final String INVALID_CREDENTIALS_EXCEPTION_MESSAGE
            = "Provided credentials are incorrect";
    private static final String INVALID_JWT_MESSAGE = "Provided JWT is invalid";

    @BeforeEach
    void setUp() {
        underTest = new AuthServiceImpl(userService, jwtProvider,
                bCryptPasswordEncoder, refreshTokenRepository);
        ReflectionTestUtils.setField(underTest, "invalidCredentialsExceptionMessage",
                INVALID_CREDENTIALS_EXCEPTION_MESSAGE);
        ReflectionTestUtils.setField(underTest, "invalidJwtMessage", INVALID_JWT_MESSAGE);
    }

    @Test
    public void canLogin() {
        // given
        String userEmail = "email@email.com";
        JwtRequest request = new JwtRequest();
        request.setEmail(userEmail);
        request.setPassword("password");
        User user = generateTestUser();
        BDDMockito.given(userService.getUserByEmail(userEmail)).willReturn(user);
        BDDMockito.given(bCryptPasswordEncoder.matches(BDDMockito.any(), BDDMockito.any())).willReturn(true);
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        BDDMockito.given(jwtProvider.generateAccessToken(user)).willReturn(accessToken);
        BDDMockito.given(jwtProvider.generateRefreshToken(user)).willReturn(refreshToken);
        List<RefreshToken> tokens = List.of(new RefreshToken(userEmail, "previous token"));
        BDDMockito.given(refreshTokenRepository.findByUserEmail(userEmail)).willReturn(tokens);
        JwtResponse expected = new JwtResponse(accessToken, refreshToken);

        // when
        JwtResponse actual = underTest.login(request);

        // then
        BDDMockito.verify(refreshTokenRepository).deleteAll(tokens);
        BDDMockito.verify(refreshTokenRepository).save(new RefreshToken(userEmail, refreshToken));
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowAuthorizationExceptionGivenInvalidCredentials() {
        // given
        String userEmail = "email@email.com";
        JwtRequest request = new JwtRequest(userEmail, "password");
        User user = generateTestUser();
        BDDMockito.given(userService.getUserByEmail(userEmail)).willReturn(user);
        BDDMockito.given(bCryptPasswordEncoder.matches(BDDMockito.any(), BDDMockito.any())).willReturn(false);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.login(request))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage(INVALID_CREDENTIALS_EXCEPTION_MESSAGE);
    }

    @Test
    public void canRefresh() {
        // given
        String userEmail = "email@email.com";
        String refreshToken = "refreshToken";
        BDDMockito.given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(true);
        Claims claims = new DefaultClaims();
        claims.setSubject(userEmail);
        BDDMockito.given(jwtProvider.getRefreshClaims(refreshToken)).willReturn(claims);
        List<RefreshToken> tokens = List.of(new RefreshToken(userEmail, refreshToken));
        BDDMockito.given(refreshTokenRepository.findByUserEmail(userEmail))
                .willReturn(tokens);
        User user = generateTestUser();
        BDDMockito.given(userService.getUserByEmail(userEmail)).willReturn(user);
        String newAccessToken = "accessToken";
        String newRefreshToken = "refreshToken";
        BDDMockito.given(jwtProvider.generateAccessToken(user)).willReturn(newAccessToken);
        BDDMockito.given(jwtProvider.generateRefreshToken(user)).willReturn(newRefreshToken);
        JwtResponse expected = new JwtResponse(newAccessToken, newRefreshToken);

        // when
        JwtResponse actual = underTest.refresh(refreshToken);

        // then
        Assertions.assertThat(actual).isEqualTo(expected);
        BDDMockito.verify(refreshTokenRepository).delete(tokens.get(0));
        BDDMockito.verify(refreshTokenRepository).save(new RefreshToken(userEmail, refreshToken));
    }

    @Test
    public void shouldThrowAuthorizationExceptionGivenInvalidRefreshToken() {
        // given
        String refreshToken = "refreshToken";
        BDDMockito.given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(false);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.refresh(refreshToken))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage(INVALID_JWT_MESSAGE);
    }

    @Test
    public void shouldThrowAuthorizationExceptionGivenTokenNotSavedInStorage() {
        // given
        String userEmail = "email@email.com";
        String refreshToken = "refreshToken";
        BDDMockito.given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(true);
        Claims claims = new DefaultClaims();
        claims.setSubject(userEmail);
        BDDMockito.given(jwtProvider.getRefreshClaims(refreshToken)).willReturn(claims);
        BDDMockito.given(refreshTokenRepository.findByUserEmail(userEmail))
                .willReturn(Collections.emptyList());

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.refresh(refreshToken))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage(INVALID_JWT_MESSAGE);
    }

    @Test
    public void shouldThrowAuthorizationExceptionIfTokensDontMatch() {
        // given
        String userEmail = "email@email.com";
        String refreshToken = "refreshToken";
        BDDMockito.given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(true);
        Claims claims = new DefaultClaims();
        claims.setSubject(userEmail);
        BDDMockito.given(jwtProvider.getRefreshClaims(refreshToken)).willReturn(claims);
        List<RefreshToken> tokens = List.of(new RefreshToken(userEmail, "previous token"));
        BDDMockito.given(refreshTokenRepository.findByUserEmail(userEmail))
                .willReturn(tokens);

        // when
        // then
        Assertions.assertThatThrownBy(() -> underTest.refresh(refreshToken))
                .isInstanceOf(AuthorizationException.class)
                .hasMessage(INVALID_JWT_MESSAGE);
    }

    private User generateTestUser() {
        return new User(UUID.randomUUID(), "username", "email@email.com", "full name",
                "password", Role.MANAGER, null, null, null);
    }
}
