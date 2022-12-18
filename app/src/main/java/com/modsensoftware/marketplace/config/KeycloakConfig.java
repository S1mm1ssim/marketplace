package com.modsensoftware.marketplace.config;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author andrey.demyanchik on 12/8/2022
 */
@Configuration
public class KeycloakConfig {

    @Value("${idm.realm-name}")
    private String realmName;
    @Value("${idm.server-url}")
    private String serverUrl;
    @Value("${idm.client-id}")
    private String clientId;
    @Value("${idm.client-secret}")
    private String clientSecret;
    @Value("${idm.admin-username}")
    private String adminUsername;
    @Value("${idm.admin-password}")
    private String adminPassword;
    @Value("${idm.pool-size}")
    private int connectionPoolSize;

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
                        .connectionPoolSize(connectionPoolSize)
                        .build()
                )
                .build();
    }
}
