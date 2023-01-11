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
 * @author andrey.demyanchik on 12/21/2022
 */
public class CompanyStubs {

    public static void setupGetAllCompanyMockResponse(WireMockServer mockServer) throws IOException {
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/companies/"))
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody((StreamUtils.copyToString(
                                        CompanyStubs.class.getClassLoader()
                                                .getResourceAsStream("stubs/get-companies-response.json"),
                                        defaultCharset())
                                ))
                ));
    }

    public static void setupGetCompanyWithId(WireMockServer mockServer, long companyId) throws IOException {
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/companies/" + companyId))
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody((StreamUtils.copyToString(
                                        CompanyStubs.class.getClassLoader()
                                                .getResourceAsStream(
                                                        format("stubs/get-company-%s-response.json", companyId)
                                                ),
                                        defaultCharset())
                                ))
                ));
    }

    public static void setupGetNonExistentCompany(WireMockServer mockServer, long companyId) {
        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/v1/companies/" + companyId))
                .willReturn(WireMock.notFound()));
    }
}
