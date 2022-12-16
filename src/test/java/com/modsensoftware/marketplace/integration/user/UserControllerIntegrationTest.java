package com.modsensoftware.marketplace.integration.user;

import com.modsensoftware.marketplace.dto.request.UserRequestDto;
import com.modsensoftware.marketplace.dto.response.UserResponseDto;
import com.modsensoftware.marketplace.exception.EntityNotFoundException;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import com.modsensoftware.marketplace.service.UserService;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.ext.ScriptUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/22/2022
 */

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    private static String accessToken;

    private static boolean wasSetupExecuted = false;
    private static int testsExecuted = 0;
    private static final int TESTS_TO_BE_EXECUTED = 9;
    private static final List<String> USER_IDS = new ArrayList<>();

    @Value("${exception.message.userNotFound}")
    private String userNotFoundMessage;
    @Value("${exception.message.userPasswordAbsent}")
    private String userPasswordAbsentMessage;

    @BeforeAll
    protected static void beforeAll() {
        AbstractIntegrationTest.beforeAll();
        accessToken = getAccessToken(TEST_MANAGER_USERNAME);
        ScriptUtils.runInitScript(dbDelegate, "integration/user/userIntegrationTestData.sql");
    }

    @AfterAll
    static void afterAll() {
        ScriptUtils.runInitScript(dbDelegate, "integration/user/userIntegrationTestTearDown.sql");
    }

    @BeforeEach
    void setUp() {
        if (!wasSetupExecuted) {
            wasSetupExecuted = true;
            USER_IDS.add(userService.createUser(createUserRequestDto("user1", "user1@user.com", 1000L)));
            USER_IDS.add(userService.createUser(createUserRequestDto("user2", "user2@user.com", 1000L)));
            USER_IDS.add(userService.createUser(createUserRequestDto("user3", "toBeDeleted@mail.com", 999L)));
            USER_IDS.add(userService.createUser(createUserRequestDto("user4", "softDeleted@user.com", 1001L)));
        }
    }

    private UserRequestDto createUserRequestDto(String username, String email, Long companyId) {
        return new UserRequestDto(username, email, "name", "password", companyId);
    }

    @AfterEach
    void tearDown() {
        testsExecuted++;
        if (testsExecuted == TESTS_TO_BE_EXECUTED) {
            USER_IDS.forEach(userId -> userService.deleteUser(UUID.fromString(userId)));
        }
    }

    @Test
    public void shouldReturn201StatusOnSaveOperation() {
        final String companyId = "999";
        Map<String, String> user = new HashMap<>();
        user.put("username", "testUser");
        user.put("email", "saveTest@test.com");
        user.put("password", "user_password");
        user.put("name", "testUser");
        user.put("companyId", companyId);
        String userId = RestAssured.given()
                .contentType("application/json")
                .when()
                .body(user)
                .post("/users").then()
                .statusCode(201).extract().response().asString();
        userService.deleteUser(UUID.fromString(userId));
    }

    @Test
    public void shouldReturnAllUsersWhoseCompanyIsNotDeleted() {
        // given
        final String userWithSoftDeletedCompany = "softDeleted@user.com";

        // when
        // then
        UserResponseDto[] users = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/users").then()
                .statusCode(200)
                .extract().body().as(UserResponseDto[].class);
        Assertions.assertThat(users.length).isGreaterThan(0);
        Assertions.assertThat(users).noneMatch(user -> userWithSoftDeletedCompany.equals(user.getEmail()));
    }

    @Test
    public void shouldReturnAllUsersFiltered() {
        // given
        int expectedUsersAmount = 2;
        String emailFilter = "user.com";
        // when
        // then
        UserResponseDto[] users = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .param("email", emailFilter)
                .when()
                .get("/users").then()
                .statusCode(200)
                .extract().body().as(UserResponseDto[].class);
        Assertions.assertThat(users.length).isEqualTo(expectedUsersAmount);
    }

    @Test
    public void canGetUserById() {
        // given
        final String userUuid = USER_IDS.get(1);
        UserResponseDto expected = userService.getUserById(UUID.fromString(userUuid));

        // when
        // then
        UserResponseDto response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(String.format("/users/%s", userUuid)).then()
                .statusCode(200)
                .extract().body().as(UserResponseDto.class);
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
                .header("Authorization", "Bearer " + accessToken)
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
        final String userUuid = USER_IDS.get(2);
        USER_IDS.remove(2);
        // when
        // then
        RestAssured.given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete(format("/users/%s", userUuid)).then()
                .statusCode(204);
        Assertions.assertThatThrownBy(() -> userService.deleteUser(UUID.fromString(userUuid)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(format(userNotFoundMessage, userUuid));
    }

    @Test
    public void canUpdateUser() {
        // given
        final String userUuid = USER_IDS.get(1);
        String updatedName = "updated name";
        String updatedFields = String.format("{\n"
                + "    \"name\": \"%s\",\n"
                + "    \"password\": \"password\"\n"
                + "}", updatedName);

        // when
        // then
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(updatedFields)
                .when()
                .put(String.format("/users/%s", userUuid)).then()
                .statusCode(200);
        UserResponseDto user = userService.getUserById(UUID.fromString(userUuid));
        Assertions.assertThat(user.getName()).isEqualTo(updatedName);
    }

    @Test
    public void shouldReturn400StatusOnUpdateWithoutPassword() {
        // given
        final String userUuid = USER_IDS.get(0);
        String updatedName = "updated name";
        String updatedFields = String.format("{\n"
                + "    \"firstName\": \"%s\"\n"
                + "}", updatedName);

        // when
        // then
        String response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(updatedFields)
                .when()
                .put(format("/users/%s", userUuid)).then()
                .statusCode(400).extract().response().asString();
        Assertions.assertThat(response).isEqualTo(userPasswordAbsentMessage);
    }
}
