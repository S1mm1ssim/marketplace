package com.modsensoftware.marketplace.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author andrey.demyanchik on 12/16/2022
 */
@Slf4j
public class CustomKeycloakContainer extends KeycloakContainer {

    private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:20.0.1";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String REALM_PATH = "realms/test-marketplace";
    private static CustomKeycloakContainer keycloakContainer;

    private CustomKeycloakContainer() {
        super(KEYCLOAK_IMAGE);
        super.withAdminUsername(ADMIN_USERNAME)
                .withAdminPassword(ADMIN_PASSWORD)
                .withRealmImportFile("keycloak/realm-export.json");
    }

    public static synchronized CustomKeycloakContainer getInstance() {
        if (keycloakContainer == null) {
            keycloakContainer = new CustomKeycloakContainer();
        }
        return keycloakContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                keycloakContainer.getAuthServerUrl() + REALM_PATH);
        System.setProperty("idm.server-url", keycloakContainer.getAuthServerUrl());
        log.info("Started Keycloak container on port {}", keycloakContainer.getHttpPort());
    }

    @Override
    public void stop() {

    }
}
