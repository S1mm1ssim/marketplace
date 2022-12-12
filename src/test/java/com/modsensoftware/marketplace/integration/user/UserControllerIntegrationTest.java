package com.modsensoftware.marketplace.integration.user;

import com.modsensoftware.marketplace.dao.UserDao;
import com.modsensoftware.marketplace.domain.User;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
 * @author andrey.demyanchik on 11/22/2022
 */

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserDao userDao;

    @Value("${exception.message.userNotFound}")
    private String userNotFoundMessage;

    @BeforeAll
    protected static void beforeAll() {
        AbstractIntegrationTest.beforeAll();
        ScriptUtils.runInitScript(dbDelegate, "integration/user/userIntegrationTestData.sql");
    }

    @AfterAll
    static void afterAll() {
        ScriptUtils.runInitScript(dbDelegate, "integration/user/userIntegrationTestTeardown.sql");
    }

    @Test
    public void shouldReturn201StatusOnSaveOperation() {
        final String companyId = "999";
        Map<String, String> user = new HashMap<>();
        user.put("username", "testUser");
        user.put("email", "saveTest@test.com");
        user.put("name", "testUser");
        user.put("companyId", companyId);
        RestAssured.given()
                .contentType("application/json")
                .when()
                .body(user)
                .post("/users").then()
                .statusCode(201);
    }

    @Test
    public void shouldReturnAllUsersWhoseCompanyIsNotDeleted() {
        // given
        final String userWithSoftDeletedCompany = "softDeleted@user.com";

        // when
        // then
        User[] users = RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/users").then()
                .statusCode(200)
                .extract().body().as(User[].class);
        Assertions.assertThat(users.length).isGreaterThan(0);
        Assertions.assertThat(users)
                .noneMatch(user -> userWithSoftDeletedCompany.equals(user.getEmail()));
    }

    @Test
    public void shouldReturnAllUsersFiltered() {
        // given
        int expectedUsersAmount = 2;
        String emailFilter = "sqluser";
        // when
        // then
        User[] users = RestAssured.given()
                .contentType("application/json")
                .param("email", emailFilter)
                .when()
                .get("/users").then()
                .statusCode(200)
                .extract().body().as(User[].class);
        Assertions.assertThat(users.length).isEqualTo(expectedUsersAmount);
    }

    @Test
    public void canGetUserById() {
        // given
        final String userUuid = "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d";
        User expected = userDao.get(UUID.fromString(userUuid));

        // when
        // then
        User response = RestAssured.given()
                .contentType("application/json")
                .when()
                .get(String.format("/users/%s", userUuid)).then()
                .statusCode(200)
                .extract().body().as(User.class);
        Assertions.assertThat(response).isEqualTo(expected);
    }

    @ValueSource(strings = {
            "c048ef0e-fe46-4c65-9c01-d88af74ba0ab",
            "c999ef9e-fe99-9c99-9c99-d99af99ba9ab"
    })
    @ParameterizedTest
    @DisplayName("Should return 404 status on getById operation if user not found or their company is soft deleted")
    public void shouldReturn404StatusOnGetByIdNotFound(String userUuid) {
        // given
        // when
        // then
        String response = RestAssured.given()
                .contentType("application/json")
                .when()
                .get(String.format("/users/%s", userUuid)).then()
                .statusCode(404)
                .extract().response().asString();
        Assertions.assertThat(response)
                .isEqualTo(format(userNotFoundMessage, userUuid));
    }

    @Test
    public void canDeleteUser() {
        // given
        final String userUuid = "722cd920-e127-4cc2-93b9-e9b4a8f18873";
        // when
        // then
        RestAssured.given()
                .when()
                .delete(String.format("/users/%s", userUuid)).then()
                .statusCode(204);
        Assertions.assertThatThrownBy(() -> userDao.get(UUID.fromString(userUuid)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void canUpdateUser() {
        // given
        final String userUuid = "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d";
        String updatedName = "updated name";
        String updatedFields = String.format("{\n"
                + "    \"name\": \"%s\"\n"
                + "}", updatedName);

        // when
        // then
        RestAssured.given()
                .contentType("application/json")
                .body(updatedFields)
                .when()
                .put(String.format("/users/%s", userUuid)).then()
                .statusCode(200);
        User user = userDao.get(UUID.fromString(userUuid));
        Assertions.assertThat(user.getName()).isEqualTo(updatedName);
    }
}
