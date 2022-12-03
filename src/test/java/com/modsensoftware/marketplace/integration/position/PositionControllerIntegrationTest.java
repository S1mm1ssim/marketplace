package com.modsensoftware.marketplace.integration.position;

import com.modsensoftware.marketplace.config.jwt.JwtProvider;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.enums.Role;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.ext.ScriptUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/23/2022
 */
public class PositionControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PositionDao positionDao;

    @Autowired
    private JwtProvider jwtProvider;

    @Value("${exception.message.positionVersionsMismatch}")
    private String positionVersionsMismatch;

    @BeforeAll
    protected static void beforeAll() {
        AbstractIntegrationTest.beforeAll();
        ScriptUtils.runInitScript(dbDelegate, "integration/position/positionIntegrationTestData.sql");
    }

    @AfterAll
    static void afterAll() {
        ScriptUtils.runInitScript(dbDelegate, "integration/position/positionIntegrationTestTearDown.sql");
    }

    @Test
    public void shouldReturn201StatusOnSaveOperation() {
        // given
        String token = generateAccessToken(getTestStorageManager());

        String itemUuid = "b6b7764c-ed62-47a4-a68d-3cad4da1e187";
        String companyId = "999";
        String userUuid = "722cd920-e127-4cc2-93b9-e9b4a8f18873";
        Map<String, String> position = new HashMap<>();
        position.put("itemId", itemUuid);
        position.put("itemVersion", "0");
        position.put("companyId", companyId);
        position.put("createdBy", userUuid);
        position.put("amount", "2");
        position.put("minAmount", "1");

        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .body(position)
                .post("/positions")
                .then().statusCode(201);
    }

    @Test
    public void shouldReturn400StatusOnSaveOperation() {
        // given
        String token = generateAccessToken(getTestStorageManager());
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
                .header("Authorization", "Bearer " + token)
                .when()
                .body(position)
                .post("/positions")
                .then().statusCode(400);
    }

    @Test
    public void shouldReturnAllPositionsWithNonSoftDeletedCompany() {
        String token = generateAccessToken(getTestManager());
        Position[] positions = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/positions")
                .then().statusCode(200)
                .extract().body().as(Position[].class);
        Assertions.assertThat(positions).noneMatch(position -> position.getCompany().getIsDeleted().equals(true));
        Assertions.assertThat(positions.length).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldReturnPositionById() {
        String positionId = "999";
        String token = generateAccessToken(getTestManager());
        Position actual = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get(format("/positions/%s", positionId))
                .then().statusCode(200)
                .extract().body().as(Position.class);

        Position expected = positionDao.get(Long.valueOf(positionId));
        expected.getCreatedBy().setPassword(null);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1001", "99999"})
    public void shouldReturn404StatusIfPositionNotFoundOrItsCompanyIsSoftDeleted(String positionId) {
        String token = generateAccessToken(getTestManager());
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .get(format("/positions/%s", positionId))
                .then().statusCode(404);
    }

    @Test
    public void shouldReturn204StatusOnDeleteOperation() {
        String positionId = "1002";
        String token = generateAccessToken(getTestStorageManager());
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(format("/positions/%s", positionId))
                .then().statusCode(204);
    }

    @Test
    public void canUpdatePosition() {
        String positionId = "999";
        String token = generateAccessToken(getTestStorageManager());
        String itemId = "b6b7764c-ed62-47a4-a68d-3cad4da1e187";
        String updatedFields = format(""
                + "{\n"
                + "    \"itemId\": \"%s\",\n"
                + "    \"amount\": 4,\n"
                + "    \"version\": 1\n"
                + "}", itemId);
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(updatedFields)
                .when()
                .put(format("/positions/%s", positionId))
                .then().statusCode(200);

        Position result = positionDao.get(Long.valueOf(positionId));
        Assertions.assertThat(result)
                .hasFieldOrPropertyWithValue("amount", 4.0)
                .hasFieldOrPropertyWithValue("version", 2L);
        Assertions.assertThat(result.getItem().getId().toString()).isEqualTo(itemId);
    }

    @Test
    public void shouldReturn400StatusOnUpdateWithIncorrectVersion() {
        String positionId = "999";
        String token = generateAccessToken(getTestStorageManager());
        Position initial = positionDao.get(Long.valueOf(positionId));
        String updatedFields = ""
                + "{\n"
                + "    \"itemId\": \"b6b7764c-ed62-47a4-a68d-3cad4da1e187\",\n"
                + "    \"amount\": 4,\n"
                + "    \"version\": 9\n"
                + "}";
        String response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(updatedFields)
                .when()
                .put(format("/positions/%s", positionId))
                .then().statusCode(400)
                .extract().response().asString();
        Assertions.assertThat(response).isEqualTo(positionVersionsMismatch);

        Position latest = positionDao.get(Long.valueOf(positionId));
        Assertions.assertThat(latest).isEqualTo(initial);
    }

    private String generateAccessToken(User user) {
        return jwtProvider.generateAccessToken(user);
    }

    private User getTestStorageManager() {
        return new User(UUID.fromString("722cd920-e127-4cc2-93b9-e9b4a8f18873"),
                "user2", "sqluser2@mail.com", "password", "full name", Role.STORAGE_MANAGER,
                null, null, null);
    }

    private User getTestManager() {
        return new User(UUID.fromString("b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d"),
                "user1", "sqluser1@mail.com", "password", "full name", Role.MANAGER,
                null, null, null);
    }
}
