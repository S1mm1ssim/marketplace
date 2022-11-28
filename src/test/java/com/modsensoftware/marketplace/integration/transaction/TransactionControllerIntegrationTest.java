package com.modsensoftware.marketplace.integration.transaction;

import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.ext.ScriptUtils;

import java.util.UUID;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/28/2022
 */
public class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

    @BeforeAll
    protected static void beforeAll() {
        AbstractIntegrationTest.beforeAll();
        ScriptUtils.runInitScript(dbDelegate, "integration/transaction/userTransactionIntegrationTestData.sql");
    }

    @AfterAll
    static void afterAll() {
        ScriptUtils.runInitScript(dbDelegate, "integration/transaction/userTransactionIntegrationTestTearDown.sql");
    }

    @Test
    public void shouldReturn201StatusOnCreateValidUserTransaction() {
        // given
        String payload = "{\n"
                + "    \"userId\": \"b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d\",\n"
                + "    \"orderLine\":\n"
                + "    [\n"
                + "        {\n"
                + "            \"positionId\": 999,\n"
                + "            \"amount\": 6,\n"
                + "            \"positionVersion\": 0\n"
                + "        }\n"
                + "    ]\n"
                + "}";

        // when
        RestAssured.given()
                .contentType("application/json")
                .when()
                .body(payload)
                .post("/users/transactions")
                .then().statusCode(201);
    }

    @Test
    public void shouldReturn400StatusOnCreateUserTransactionWithoutPositionVersion() {
        // given
        Long positionId = 999L;
        String invalidPayload = format("{\n"
                + "    \"userId\": \"b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d\",\n"
                + "    \"orderLine\":\n"
                + "    [\n"
                + "        {\n"
                + "            \"positionId\": %s,\n"
                + "            \"amount\": 6\n"
                + "        }\n"
                + "    ]\n"
                + "}", positionId);

        // when
        String response = RestAssured.given()
                .contentType("application/json")
                .when()
                .body(invalidPayload)
                .post("/users/transactions")
                .then().statusCode(400)
                .extract().response().asString();


        // then
        Assertions.assertThat(response).isEqualTo(
                format("No version for position with id %s was provided", positionId)
        );
    }

    @Test
    public void shouldReturn400StatusOnCreateUserTransactionWithTooBigAmount() {
        // given
        Long positionId = 999L;
        double amount = 100000.0;
        String invalidPayload = format("{\n"
                + "    \"userId\": \"b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d\",\n"
                + "    \"orderLine\":\n"
                + "    [\n"
                + "        {\n"
                + "            \"positionId\": %s,\n"
                + "            \"amount\": %s,\n"
                + "            \"positionVersion\": 0\n"
                + "        }\n"
                + "    ]\n"
                + "}", positionId, amount);

        // when
        String response = RestAssured.given()
                .contentType("application/json")
                .when()
                .body(invalidPayload)
                .post("/users/transactions")
                .then().statusCode(400)
                .extract().response().asString();

        // then
        Assertions.assertThat(response).contains(
                format("Not enough items in stock for position with id=%s. Wanted amount=%s.",
                        positionId, amount));
    }

    @DisplayName("Should return 400 status on creation of UserTransaction with order amount not big enough for this position")
    @Test
    public void shouldReturn400StatusOnCreateUserTransactionWithNotBigAmount() {
        Long positionId = 999L;
        double amount = 1d;
        String invalidPayload = format("{\n"
                + "    \"userId\": \"b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d\",\n"
                + "    \"orderLine\":\n"
                + "    [\n"
                + "        {\n"
                + "            \"positionId\": %s,\n"
                + "            \"amount\": %s,\n"
                + "            \"positionVersion\": 0\n"
                + "        }\n"
                + "    ]\n"
                + "}", positionId, amount);

        String response = RestAssured.given()
                .contentType("application/json")
                .when()
                .body(invalidPayload)
                .post("/users/transactions")
                .then().statusCode(400)
                .extract().response().asString();

        Assertions.assertThat(response).contains(
                format("Wanted amount=%s is less than position's(id=%s) minimum amount=",
                        amount, positionId));
    }

    @Test
    public void shouldReturnAllTransactionsForUser() {
        int expectedTransactionsAmount = 2;
        String userId = "722cd920-e127-4cc2-93b9-e9b4a8f18873";
        UserTransaction[] userTransactions = RestAssured.given()
                .contentType("application/json")
                .when()
                .get(format("/users/%s/transactions", userId))
                .then().statusCode(200)
                .extract().body().as(UserTransaction[].class);

        Assertions.assertThat(userTransactions.length).isEqualTo(expectedTransactionsAmount);
        Assertions.assertThat(userTransactions)
                .allMatch(userTransaction -> userTransaction.getUserId().equals(UUID.fromString(userId)));
    }
}
