package com.modsensoftware.marketplace.integration;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author andrey.demyanchik on 11/22/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=8081",
                "management.server.port=9081"
        })
public abstract class AbstractIntegrationTest {

    private static final int DEFAULT_PORT = 8081;
    private static final String BASE_PATH = "/api/v1";

    private static final String AUTH_PATH
            = "http://localhost:8079/realms/test-marketplace/protocol/openid-connect/token";
    private static final String CLIENT_ID = "test-idm-client";
    private static final String CLIENT_SECRET = "thqX0NUWScsc40CoUvDZaG0hImSZhMSY";
    private static final String TEST_USER_PASSWORD = "password";
    private static final String GRANT_TYPE = "password";

    protected static final String TEST_MANAGER_USERNAME = "test-manager";
    protected static final String TEST_STORAGE_MANAGER_USERNAME = "test-storage-manager";
    protected static final String TEST_DIRECTOR_USERNAME = "test-director";

    private static final RestTemplate restTemplate = new RestTemplate();

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    protected static final JdbcDatabaseDelegate dbDelegate
            = new JdbcDatabaseDelegate(postgreSQLContainer, "");

    @BeforeAll
    protected static void beforeAll() {
        String port = System.getProperty("server.port");
        if (port == null) {
            RestAssured.port = DEFAULT_PORT;
        } else {
            RestAssured.port = Integer.parseInt(port);
        }

        String basePath = System.getProperty("server.base");
        if (basePath == null) {
            basePath = BASE_PATH;
        }
        RestAssured.basePath = basePath;
    }

    protected static String getAccessToken(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", CLIENT_ID);
        map.add("username", username);
        map.add("password", TEST_USER_PASSWORD);
        map.add("grant_type", GRANT_TYPE);
        map.add("client_secret", CLIENT_SECRET);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<AccessTokenResponse> accessToken
                = restTemplate.exchange(AUTH_PATH, HttpMethod.POST, entity, AccessTokenResponse.class);
        return accessToken.getBody().getToken();
    }
}
