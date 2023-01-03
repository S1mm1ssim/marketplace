package com.modsensoftware.marketplace.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsensoftware.marketplace.dto.Company;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static java.time.LocalDateTime.now;

/**
 * @author andrey.demyanchik on 1/3/2023
 */
public class UserStubs {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public static void setupGetUserWithId(WireMockServer mockServer, String userId) throws IOException {
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/users/" + userId))
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody((StreamUtils.copyToString(
                                        UserStubs.class.getClassLoader()
                                                .getResourceAsStream(
                                                        format("stubs/get-user-%s-response.json", userId)
                                                ),
                                        defaultCharset())
                                ))
                ));
    }

    public static void setupDeterministicGetUserWithId(WireMockServer mockServer, String userId) throws IOException {
        UserResponseDto user = new UserResponseDto(UUID.fromString(userId), "test-storage-manager",
                "email@email.com", "full name", now(), now(), Company.builder().id(999L).build());
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/users/" + userId))
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(objectMapper.writeValueAsString(user))
                ));
    }

    public static void setupGetNonExistentUser(WireMockServer mockServer, String userId) {
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/users/" + userId))
                .willReturn(WireMock.notFound()));
    }
}
