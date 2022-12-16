package com.modsensoftware.marketplace.integration.transaction;

import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
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
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.ext.ScriptUtils;

import java.util.UUID;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/28/2022
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private Keycloak keycloak;

    private static String accessToken;

    @Value("${exception.message.noPositionVersionProvided}")
    private String noPositionVersionProvidedMessage;
    @Value("${exception.message.insufficientItemsInStock}")
    private String insufficientItemsInStockMessage;
    @Value("${exception.message.insufficientOrderAmount}")
    private String insufficientOrderAmountMessage;

    @BeforeAll
    protected static void beforeAll() {
        AbstractIntegrationTest.beforeAll();
        ScriptUtils.runInitScript(dbDelegate, "integration/transaction/userTransactionIntegrationTestData.sql");
        accessToken = getAccessToken(TEST_MANAGER_USERNAME);
    }

    @AfterAll
    static void afterAll() {
        ScriptUtils.runInitScript(dbDelegate, "integration/transaction/userTransactionIntegrationTestTearDown.sql");
    }

    @BeforeEach
    void setUp() {
        OrderArgumentsProvider.insufficientItemsInStockMessage = this.insufficientItemsInStockMessage;
        OrderArgumentsProvider.insufficientOrderAmountMessage = this.insufficientOrderAmountMessage;
        OrderArgumentsProvider.noPositionVersionProvidedMessage = this.noPositionVersionProvidedMessage;
    }

    @Order(1)
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
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(payload)
                .post("/users/transactions")
                .then().statusCode(201);
    }

    @Order(2)
    @ParameterizedTest
    @ArgumentsSource(OrderArgumentsProvider.class)
    public void shouldReturn400StatusOnCreateTransactionWithInvalidOrder(Long positionId,
                                                                         Double amount,
                                                                         Long positionVersion,
                                                                         String exceptionMessage) {
        // given
        String invalidPayload = format("{\n"
                + "    \"userId\": \"b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d\",\n"
                + "    \"orderLine\":\n"
                + "    [\n"
                + "        {\n"
                + "            \"positionId\": %s,\n"
                + "            \"amount\": %s,\n"
                + "            \"positionVersion\": %s\n"
                + "        }\n"
                + "    ]\n"
                + "}", positionId, amount, positionVersion);

        // when
        String response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(invalidPayload)
                .post("/users/transactions")
                .then().statusCode(400)
                .extract().response().asString();


        // then
        Assertions.assertThat(response).isEqualTo(exceptionMessage);
    }

    @Test
    public void shouldReturnAllTransactionsForUser() {
        int expectedTransactionsAmount = 2;
        String userId = "722cd920-e127-4cc2-93b9-e9b4a8f18873";
        UserTransaction[] userTransactions = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(format("/users/%s/transactions", userId))
                .then().statusCode(200)
                .extract().body().as(UserTransaction[].class);

        Assertions.assertThat(userTransactions.length).isEqualTo(expectedTransactionsAmount);
        Assertions.assertThat(userTransactions)
                .allMatch(userTransaction -> userTransaction.getUserId().equals(UUID.fromString(userId)));
    }
}
