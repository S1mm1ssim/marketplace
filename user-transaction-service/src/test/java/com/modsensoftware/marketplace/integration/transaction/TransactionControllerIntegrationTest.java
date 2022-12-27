package com.modsensoftware.marketplace.integration.transaction;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsensoftware.marketplace.domain.UserTransaction;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import com.modsensoftware.marketplace.integration.LoadBalancerTestConfig;
import com.modsensoftware.marketplace.integration.PositionStubs;
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
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.ext.ScriptUtils;

import java.io.IOException;
import java.util.UUID;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 11/28/2022
 */
@ActiveProfiles({"integration-test", "wiremock-test"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = {LoadBalancerTestConfig.class})
public class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WireMockServer wireMockServer1;
    @Autowired
    private WireMockServer wireMockServer2;

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
        ScriptUtils.runInitScript(AbstractIntegrationTest.dbDelegate, "integration/transaction/userTransactionIntegrationTestData.sql");
    }

    @AfterAll
    static void afterAll() {
        ScriptUtils.runInitScript(AbstractIntegrationTest.dbDelegate, "integration/transaction/userTransactionIntegrationTestTearDown.sql");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = this.port;
        accessToken = getAccessToken(AbstractIntegrationTest.TEST_MANAGER_USERNAME);
        OrderArgumentsProvider.insufficientItemsInStockMessage = this.insufficientItemsInStockMessage;
        OrderArgumentsProvider.insufficientOrderAmountMessage = this.insufficientOrderAmountMessage;
        OrderArgumentsProvider.noPositionVersionProvidedMessage = this.noPositionVersionProvidedMessage;
    }

    @Order(1)
    @Test
    public void shouldReturn201StatusOnCreateValidUserTransaction() throws IOException {
        // given
        UserStubs.setupGetUserById(wireMockServer1, "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d");
        UserStubs.setupGetUserById(wireMockServer2, "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d");
        PositionStubs.setupGetPositionById(wireMockServer1, 999L);
        PositionStubs.setupGetPositionById(wireMockServer2, 999L);
        PositionStubs.setupPutPosition(wireMockServer1, 999L);
        PositionStubs.setupPutPosition(wireMockServer2, 999L);

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

    @Test
    public void shouldLoadBalanceFeignGetPositionAndUser() throws IOException {
        // given
        UserStubs.setupGetUserById(wireMockServer1, "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d");
        UserStubs.setupGetUserById(wireMockServer2, "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d");
        PositionStubs.setupGetPositionById(wireMockServer1, 999L);
        PositionStubs.setupGetPositionById(wireMockServer2, 999L);
        PositionStubs.setupPutPosition(wireMockServer1, 999L);
        PositionStubs.setupPutPosition(wireMockServer2, 999L);
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
        for (int i = 0; i < 10; i++) {
            RestAssured.given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .when()
                    .body(payload)
                    .post("/users/transactions")
                    .then().statusCode(201);
        }

        // then
        wireMockServer1.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/users/b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d")));
        wireMockServer2.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/users/b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d")));
        wireMockServer1.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/positions/999")));
        wireMockServer2.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/positions/999")));
    }

    @Order(2)
    @ParameterizedTest
    @ArgumentsSource(OrderArgumentsProvider.class)
    public void shouldReturn400StatusOnCreateTransactionWithInvalidOrder(Long positionId,
                                                                         Double amount,
                                                                         Long positionVersion,
                                                                         String exceptionMessage) throws IOException {
        // given
        UserStubs.setupGetUserById(wireMockServer1, "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d");
        UserStubs.setupGetUserById(wireMockServer2, "b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d");
        PositionStubs.setupGetPositionById(wireMockServer1, 999L);
        PositionStubs.setupGetPositionById(wireMockServer2, 999L);
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
