package com.modsensoftware.marketplace.config;

import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author andrey.demyanchik on 12/8/2022
 */
@Configuration
@ConfigurationProperties(prefix = "idm")
@ConfigurationPropertiesScan
@ConstructorBinding
@RequiredArgsConstructor
public class KeycloakConfig {

    private final String realmName;
    private final String serverUrl;
    private final String clientId;
    private final String clientSecret;
    private final String adminUsername;
    private final String adminPassword;
    private final int poolSize;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realmName)
                .grantType(OAuth2Constants.PASSWORD)
                .username(adminUsername)
                .password(adminPassword)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(poolSize)
                        .build()
                )
                .build();
    }
}
