package com.modsensoftware.marketplace.integration;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import com.modsensoftware.marketplace.RedisContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    private static final String BASE_PATH = "/api/v1";

    private static final String GET_ACCESS_TOKEN_PATH = "/protocol/openid-connect/token";

    protected static final String TEST_MANAGER_USERNAME = "test-manager";
    protected static final String TEST_STORAGE_MANAGER_USERNAME = "test-storage-manager";
    protected static final String TEST_DIRECTOR_USERNAME = "test-director";
    private static final String TEST_USER_PASSWORD = "password";

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    @Value("${idm.client-id}")
    private String clientId;
    @Value("${idm.client-secret}")
    private String clientSecret;
    @Value("${idm.grant-type}")
    private String grantType;

    private static final RestTemplate restTemplate = new RestTemplate();

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    @Container
    public static CustomKeycloakContainer keycloakContainer
            = CustomKeycloakContainer.getInstance();

    @Container
    public static RedisContainer redisContainer = RedisContainer.getInstance();

    protected static final JdbcDatabaseDelegate dbDelegate
            = new JdbcDatabaseDelegate(postgreSQLContainer, "");

    @BeforeAll
    protected static void beforeAll() {
        String basePath = System.getProperty("server.base");
        if (basePath == null) {
            basePath = BASE_PATH;
        }
        RestAssured.basePath = basePath;
    }

    protected String getAccessToken(String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("username", username);
        map.add("password", TEST_USER_PASSWORD);
        map.add("client_secret", clientSecret);
        map.add("grant_type", grantType);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<AccessTokenResponse> accessToken
                = restTemplate.exchange(issuerUri + GET_ACCESS_TOKEN_PATH, HttpMethod.POST,
                entity, AccessTokenResponse.class);
        return accessToken.getBody().getToken();
    }
}
