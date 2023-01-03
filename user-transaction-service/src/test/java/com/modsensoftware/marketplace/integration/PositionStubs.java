package com.modsensoftware.marketplace.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;

import java.io.IOException;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

/**
 * @author andrey.demyanchik on 12/27/2022
 */
public class PositionStubs {

    public static void setupGetPositionById(WireMockServer mockServer, long positionId) throws IOException {
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/positions/" + positionId))
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody((StreamUtils.copyToString(
                                        PositionStubs.class.getClassLoader()
                                                .getResourceAsStream(
                                                        format("stubs/get-position-%s-response.json", positionId)
                                                ),
                                        defaultCharset())
                                ))
                ));
    }
}
