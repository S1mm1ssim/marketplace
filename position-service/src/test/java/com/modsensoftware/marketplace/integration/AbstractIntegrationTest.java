package com.modsensoftware.marketplace.integration;

import com.modsensoftware.marketplace.CustomMongoContainer;
import com.modsensoftware.marketplace.RedisContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author andrey.demyanchik on 11/22/2022
 */
@Testcontainers
@EmbeddedKafka(topics = {"userTransactionStatusResultsTest", "userTransactionProcessingTest"})
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

    @Autowired
    private IdmClientProperties idmClient;

    private static final WebClient webClient = WebClient.create();

    @Container
    public static CustomMongoContainer mongoContainer
            = CustomMongoContainer.getInstance();

    @Container
    public static CustomKeycloakContainer keycloakContainer
            = CustomKeycloakContainer.getInstance();

    @Container
    public static RedisContainer redisContainer = RedisContainer.getInstance();

    @BeforeAll
    protected static void beforeAll() {
        String basePath = System.getProperty("server.base");
        if (basePath == null) {
            basePath = BASE_PATH;
        }
        RestAssured.basePath = basePath;
    }

    protected String getAccessToken(String username) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", idmClient.getClientId());
        formData.add("username", username);
        formData.add("password", TEST_USER_PASSWORD);
        formData.add("client_secret", idmClient.getClientSecret());
        formData.add("grant_type", idmClient.getGrantType());

        AccessTokenResponse accessTokenResponse = webClient
                .method(HttpMethod.POST)
                .uri(issuerUri + GET_ACCESS_TOKEN_PATH)
                .bodyValue(formData)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .block();
        return accessTokenResponse.getToken();
    }
}
