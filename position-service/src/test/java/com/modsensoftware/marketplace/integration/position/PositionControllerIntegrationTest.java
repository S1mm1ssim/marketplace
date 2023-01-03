package com.modsensoftware.marketplace.integration.position;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import com.modsensoftware.marketplace.integration.CompanyStubs;
import com.modsensoftware.marketplace.integration.LoadBalancerTestConfig;
import com.modsensoftware.marketplace.integration.UserStubs;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.ext.ScriptUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/23/2022
 */
@ActiveProfiles("wiremock-test")
@ContextConfiguration(classes = {LoadBalancerTestConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PositionControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WireMockServer wireMockServer1;
    @Autowired
    private WireMockServer wireMockServer2;
    @Autowired
    private WireMockServer wireMockServer3;
    @Autowired
    private WireMockServer wireMockServer4;
    @Autowired
    private PositionDao positionDao;
    @Autowired
    private JwtDecoder decoder;

    private static String accessToken;
    private static String savedPositionId;

    @BeforeAll
    protected static void beforeAll() {
        AbstractIntegrationTest.beforeAll();
        ScriptUtils.runInitScript(dbDelegate, "integration/position/positionIntegrationTestData.sql");
    }

    @AfterAll
    static void afterAll() {
        ScriptUtils.runInitScript(dbDelegate, "integration/position/positionIntegrationTestTearDown.sql");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = this.port;
        accessToken = getAccessToken(TEST_STORAGE_MANAGER_USERNAME);
    }

    @Order(1)
    @Test
    public void shouldReturn201StatusOnSaveOperation() throws IOException {
        // given
        Jwt jwt = decoder.decode(accessToken);
        String userId = jwt.getClaim("sub");
        UserStubs.setupDeterministicGetUserWithId(wireMockServer3, userId);
        UserStubs.setupDeterministicGetUserWithId(wireMockServer4, userId);

        String itemUuid = "b6b7764c-ed62-47a4-a68d-3cad4da1e187";
        Map<String, String> position = new HashMap<>();
        position.put("itemId", itemUuid);
        position.put("itemVersion", "0");
        position.put("amount", "2");
        position.put("minAmount", "1");

        PositionControllerIntegrationTest.savedPositionId = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(position)
                .post("/positions")
                .then().statusCode(201).extract().body().asString();
    }

    @Order(2)
    @Test
    public void canUpdatePosition() {
        String positionId = savedPositionId;
        String updatedFields = ""
                + "{\n"
                + "    \"amount\": 4\n"
                + "}";
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(updatedFields)
                .when()
                .put(format("/positions/%s", positionId))
                .then().statusCode(200);

        Position result = positionDao.get(Long.valueOf(positionId));
        Assertions.assertThat(result.getAmount()).isEqualTo(4.0);
    }

    @Order(3)
    @Test
    public void shouldReturn204StatusOnDeleteOperation() {
        String positionId = savedPositionId;
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete(format("/positions/%s", positionId))
                .then().statusCode(204);
    }

    @Test
    public void shouldReturn400StatusOnSaveOperation() {
        // given
        String itemUuid = "b6b7764c-ed62-47a4-a68d-3cad4da1e187";
        Map<String, String> position = new HashMap<>();
        // No item version in params
        position.put("itemId", itemUuid);
        position.put("amount", "2");
        position.put("minAmount", "1");

        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(position)
                .post("/positions")
                .then().statusCode(400);
    }

    @Test
    public void shouldReturnAllPositionsWithNonSoftDeletedCompany() throws IOException {
        CompanyStubs.setupGetAllCompanyMockResponse(wireMockServer1);
        CompanyStubs.setupGetAllCompanyMockResponse(wireMockServer2);
        UserStubs.setupGetUserWithId(wireMockServer3, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetUserWithId(wireMockServer4, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetUserWithId(wireMockServer3, "722cd920-e127-4cc2-93b9-e9b4a8f18873");
        UserStubs.setupGetUserWithId(wireMockServer4, "722cd920-e127-4cc2-93b9-e9b4a8f18873");
        PositionResponseDto[] positions = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/positions")
                .then().statusCode(200)
                .extract().body().as(PositionResponseDto[].class);
        Assertions.assertThat(positions.length).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldLoadBalanceGetAllPositions() throws IOException {
        CompanyStubs.setupGetAllCompanyMockResponse(wireMockServer1);
        CompanyStubs.setupGetAllCompanyMockResponse(wireMockServer2);
        UserStubs.setupGetUserWithId(wireMockServer3, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetUserWithId(wireMockServer4, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetUserWithId(wireMockServer3, "722cd920-e127-4cc2-93b9-e9b4a8f18873");
        UserStubs.setupGetUserWithId(wireMockServer4, "722cd920-e127-4cc2-93b9-e9b4a8f18873");
        for (int i = 0; i < 10; i++) {
            RestAssured.given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .when()
                    .get("/positions")
                    .then().statusCode(200);
        }
        wireMockServer1.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/companies/")));
        wireMockServer2.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/companies/")));
    }

    @Test
    public void shouldLoadBalanceGetPositionById() throws IOException {
        String userId = "722cd920-e127-4cc2-93b9-e9b4a8f18873";
        UserStubs.setupGetUserWithId(wireMockServer3, userId);
        UserStubs.setupGetUserWithId(wireMockServer4, userId);
        String positionId = "999";
        for (int i = 0; i < 10; i++) {
            RestAssured.given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .when()
                    .get(format("/positions/%s", positionId))
                    .then().statusCode(200);
        }
        wireMockServer3.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/users/" + userId)));
        wireMockServer4.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/users/" + userId)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999", "1001"})
    public void shouldReturn404StatusIfPositionNotFoundOrCreatorUserNotFound(String positionId) {
        UserStubs.setupGetNonExistentUser(wireMockServer3, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetNonExistentUser(wireMockServer4, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(format("/positions/%s", positionId))
                .then().statusCode(404);
    }

    @Test
    public void deleteByAnotherPersonShouldReturnForbidden() {
        String positionId = "1002";
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete(format("/positions/%s", positionId))
                .then().statusCode(403);
    }

    @Test
    public void updateByAnotherPersonShouldReturnForbidden() {
        String positionId = "999";
        String updatedFields = ""
                + "{\n"
                + "    \"amount\": 4,\n"
                + "    \"version\": 1\n"
                + "}";
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(updatedFields)
                .when()
                .put(format("/positions/%s", positionId))
                .then().statusCode(403);
    }
}
