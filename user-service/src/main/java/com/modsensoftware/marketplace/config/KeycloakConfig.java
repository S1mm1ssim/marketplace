package com.modsensoftware.marketplace.config;

import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author andrey.demyanchik on 12/8/2022
 */
@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final KeycloakConfigProperties properties;

    @Bean
    public Keycloak keycloak() {
        System.out.println(properties);
        return KeycloakBuilder.builder()
                .serverUrl(properties.getServerUrl())
                .realm(properties.getRealmName())
                .grantType(OAuth2Constants.PASSWORD)
                .username(properties.getAdminUsername())
                .password(properties.getAdminPassword())
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(properties.getPoolSize())
                        .build()
                )
                .build();
    }
}
