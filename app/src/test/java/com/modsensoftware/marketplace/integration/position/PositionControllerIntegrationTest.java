package com.modsensoftware.marketplace.integration.position;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.request.UserRequest;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import com.modsensoftware.marketplace.integration.CompanyStubs;
import com.modsensoftware.marketplace.integration.LoadBalancerTestConfig;
import com.modsensoftware.marketplace.service.UserService;
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
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.ext.ScriptUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/23/2022
 */
@ActiveProfiles({"wiremock-test", "integration-test"})
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
    private PositionDao positionDao;
    @Autowired
    private UserService userService;
    @Autowired
    private Keycloak keycloak;

    private static String accessToken;
    private static String savedPositionId;

    private static final String POSITION_MANAGER_USERNAME = "position-manager";
    private static boolean wasStorageManagerRegistered = false;
    @Value("${idm.realm-name}")
    private String realmName;

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
    void setUp() throws IOException {
        RestAssured.port = this.port;
        if (!wasStorageManagerRegistered) {
            CompanyStubs.setupGetCompanyWithId(wireMockServer1, 999L);
            CompanyStubs.setupGetCompanyWithId(wireMockServer2, 999L);
            // Saving new user
            String userId = userService.createUser(new UserRequest(POSITION_MANAGER_USERNAME, "storage_manager@user.com",
                    "full name", "password", 999L));
            // Adding STORAGE_MANAGER role
            UserResource userResource = keycloak.realm(realmName).users().get(userId);
            RoleRepresentation storageManagerRole = keycloak.realm(realmName).roles()
                    .get("STORAGE_MANAGER").toRepresentation();
            userResource.roles().realmLevel().add(List.of(storageManagerRole));
            wasStorageManagerRegistered = true;
        }
        accessToken = getAccessToken(TEST_STORAGE_MANAGER_USERNAME);
    }

    @Order(1)
    @Test
    public void shouldReturn201StatusOnSaveOperation() throws IOException {
        accessToken = getAccessToken(POSITION_MANAGER_USERNAME);
        CompanyStubs.setupGetCompanyWithId(wireMockServer1, 999L);
        CompanyStubs.setupGetCompanyWithId(wireMockServer2, 999L);
        // given
        String itemUuid = "b6b7764c-ed62-47a4-a68d-3cad4da1e187";
        String companyId = "999";
        Map<String, String> position = new HashMap<>();
        position.put("itemId", itemUuid);
        position.put("itemVersion", "0");
        position.put("companyId", companyId);
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
        accessToken = getAccessToken(POSITION_MANAGER_USERNAME);
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
        accessToken = getAccessToken(POSITION_MANAGER_USERNAME);
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
        String companyId = "999";
        String userUuid = "722cd920-e127-4cc2-93b9-e9b4a8f18873";
        Map<String, String> position = new HashMap<>();
        // No item version in params
        position.put("itemId", itemUuid);
        position.put("companyId", companyId);
        position.put("createdBy", userUuid);
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
        PositionResponse[] positions = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/positions")
                .then().statusCode(200)
                .extract().body().as(PositionResponse[].class);
        Assertions.assertThat(positions.length).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldLoadBalanceGetAllPositions() throws IOException {
        CompanyStubs.setupGetAllCompanyMockResponse(wireMockServer1);
        CompanyStubs.setupGetAllCompanyMockResponse(wireMockServer2);
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
        long companyId = 1000L;
        CompanyStubs.setupGetCompanyWithId(wireMockServer1, companyId);
        CompanyStubs.setupGetCompanyWithId(wireMockServer2, companyId);
        String positionId = "999";
        for (int i = 0; i < 10; i++) {
            RestAssured.given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .when()
                    .get(format("/positions/%s", positionId))
                    .then().statusCode(200);
        }
        wireMockServer1.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/companies/" + companyId)));
        wireMockServer2.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/companies/" + companyId)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999", "1001"})
    public void shouldReturn404StatusIfPositionNotFoundOrItsCompanyIsSoftDeleted(String positionId) {
        // createdBy.company
        CompanyStubs.setupGetNonExistentCompany(wireMockServer1, 1001L);
        CompanyStubs.setupGetNonExistentCompany(wireMockServer2, 1001L);
        // company_id
        CompanyStubs.setupGetNonExistentCompany(wireMockServer1, 1002L);
        CompanyStubs.setupGetNonExistentCompany(wireMockServer2, 1002L);
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
