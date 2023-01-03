package com.modsensoftware.marketplace.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import java.io.IOException;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

/**
 * @author andrey.demyanchik on 1/3/2023
 */
public class UserStubs {

    public static void setupGetUserWithId(WireMockServer mockServer, String userId) throws IOException {
//        StreamUtils.copyToString(
//                CompanyStubs.class.getClassLoader()
//                        .getResourceAsStream(
//                                format("stubs/get-company-%s-response.json", userId)
//                        ),
//                defaultCharset());

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        UserResponseDto userResponseDto = new UserResponseDto();
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/users/" + userId))
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("")
                ));
    }
}
