package com.modsensoftware.marketplace.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author andrey.demyanchik on 12/22/2022
 */
@TestConfiguration
@ActiveProfiles("wiremock-test")
public class LoadBalancerTestConfig {

    private static final int WIREMOCK_PORT_1 = 9561;
    private static final int WIREMOCK_PORT_2 = 9562;

    @Bean(name = "wireMockServer1", initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer1() {
        return new WireMockServer(WIREMOCK_PORT_1);
    }

    @Bean(name = "wireMockServer2", initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer2() {
        return new WireMockServer(WIREMOCK_PORT_2);
    }
}
