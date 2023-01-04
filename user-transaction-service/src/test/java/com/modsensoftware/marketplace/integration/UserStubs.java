package com.modsensoftware.marketplace.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import java.io.IOException;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

/**
 * @author andrey.demyanchik on 12/27/2022
 */
public class UserStubs {

    public static void setupGetUserById(WireMockServer mockServer, String userId) throws IOException {
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/users/" + userId))
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody((StreamUtils.copyToString(
                                        PositionStubs.class.getClassLoader()
                                                .getResourceAsStream(
                                                        format("stubs/get-position-%s-response.json", userId)
                                                ),
                                        defaultCharset())
                                ))
                ));
    }
}
