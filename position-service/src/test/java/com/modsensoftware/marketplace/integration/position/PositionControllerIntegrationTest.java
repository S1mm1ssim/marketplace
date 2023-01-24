package com.modsensoftware.marketplace.integration.position;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsensoftware.marketplace.dao.PositionDao;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.dto.response.PositionResponse;
import com.modsensoftware.marketplace.integration.AbstractIntegrationTest;
import com.modsensoftware.marketplace.integration.LoadBalancerTestConfig;
import com.modsensoftware.marketplace.integration.UserStubs;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.modsensoftware.marketplace.constants.Constants.MONGO_ID_FIELD_NAME;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;

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
    private PositionDao positionDao;
    @Autowired
    private ReactiveJwtDecoder decoder;
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    private static String accessToken;
    private static String savedPositionId;

    private static boolean werePositionsSetUp = false;

    @BeforeAll
    protected static void beforeAll() {
        AbstractIntegrationTest.beforeAll();
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = this.port;
        accessToken = getAccessToken(TEST_STORAGE_MANAGER_USERNAME);
        if (!werePositionsSetUp) {
            setupPositions();
            werePositionsSetUp = true;
        }
    }

    @Order(1)
    @Test
    public void shouldReturn201StatusOnSaveOperation() throws IOException {
        // given
        Mono<Jwt> jwt = decoder.decode(accessToken);
        String userId = (String) jwt.map(token -> token.getClaim("sub")).block();
        UserStubs.setupDeterministicGetUserWithId(wireMockServer1, userId);
        UserStubs.setupDeterministicGetUserWithId(wireMockServer2, userId);

        String itemUuid = "12347";
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
                .then().statusCode(201).extract().body().as(Position.class).getId();
    }

    @Order(2)
    @Test
    public void canUpdatePosition() {
        String positionId = savedPositionId;
        Map<String, String> updatedFields = new HashMap<>();
        updatedFields.put("amount", "4");
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(updatedFields)
                .when()
                .put(format("/positions/%s", positionId))
                .then().statusCode(200);

        positionDao.get(positionId).as(StepVerifier::create)
                .expectNextMatches(position -> position.getAmount().equals(4.0))
                .expectComplete()
                .verify();
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

    @Order(4)
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

    @Order(5)
    @Test
    public void shouldReturnAllPositionsWithNonSoftDeletedCompany() throws IOException {
        UserStubs.setupGetUserWithId(wireMockServer1, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetUserWithId(wireMockServer2, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetUserWithId(wireMockServer1, "722cd920-e127-4cc2-93b9-e9b4a8f18873");
        UserStubs.setupGetUserWithId(wireMockServer2, "722cd920-e127-4cc2-93b9-e9b4a8f18873");
        PositionResponseDto[] positions = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/positions")
                .then().statusCode(200)
                .extract().body().as(PositionResponse[].class);
        Assertions.assertThat(positions.length).isGreaterThanOrEqualTo(2);
    }

    @Order(6)
    @Test
    public void shouldLoadBalanceGetAllPositions() throws IOException {
        UserStubs.setupGetUserWithId(wireMockServer1, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetUserWithId(wireMockServer2, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetUserWithId(wireMockServer1, "722cd920-e127-4cc2-93b9-e9b4a8f18873");
        UserStubs.setupGetUserWithId(wireMockServer2, "722cd920-e127-4cc2-93b9-e9b4a8f18873");
        for (int i = 0; i < 10; i++) {
            RestAssured.given()
                    .contentType("application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .when()
                    .get("/positions")
                    .then().statusCode(200);
        }
        wireMockServer1.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/users/722cd920-e127-4cc2-93b9-e9b4a8f18873")));
        wireMockServer2.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/users/722cd920-e127-4cc2-93b9-e9b4a8f18873")));

    }

    @Order(7)
    @Test
    public void shouldLoadBalanceGetPositionById() throws IOException {
        String userId = "722cd920-e127-4cc2-93b9-e9b4a8f18873";
        UserStubs.setupGetUserWithId(wireMockServer1, userId);
        UserStubs.setupGetUserWithId(wireMockServer2, userId);
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
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/users/" + userId)));
        wireMockServer2.verify(WireMock.moreThan(0),
                WireMock.getRequestedFor(WireMock.urlEqualTo("/api/v1/users/" + userId)));
    }

    @Order(8)
    @ParameterizedTest
    @ValueSource(strings = {"99999", "1001"})
    public void shouldReturn404StatusIfPositionNotFoundOrCreatorUserNotFound(String positionId) {
        UserStubs.setupGetNonExistentUser(wireMockServer1, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        UserStubs.setupGetNonExistentUser(wireMockServer2, "c048ef0e-fe46-4c65-9c01-d88af74ba0ab");
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get(format("/positions/%s", positionId))
                .then().statusCode(404);
    }

    @Order(9)
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

    @Order(10)
    @Test
    public void updateByAnotherPersonShouldReturnForbidden() {
        String positionId = "999";
        Map<String, String> updatedFields = new HashMap<>();
        updatedFields.put("amount", "4");
        updatedFields.put("version", "1");
        RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + accessToken)
                .body(updatedFields)
                .when()
                .put(format("/positions/%s", positionId))
                .then().statusCode(403);

        // Deleting all setup data
        tearDownPositions();
    }

    private void setupPositions() {
        Category parent = Category.builder().id("999").name("root")
                .description("description").build();
        Category category = Category.builder().id("1000").name("electronics").parent(parent)
                .description("electronics").build();
        Item item1 = Item.builder().id("12345").name("item1").description("description")
                .created(now()).category(category).build();
        Item item3 = Item.builder().id("12347").name("item3").description("description")
                .created(now()).category(category).build();
        mongoTemplate.save(item3).block();
        positionDao.save(Position.builder()
                .id("999").item(item1)
                .companyId(1000L).createdBy("722cd920-e127-4cc2-93b9-e9b4a8f18873")
                .created(now()).amount(150d).minAmount(0.1d)
                .build()).block();
        positionDao.save(Position.builder()
                .id("1002").item(item1)
                .companyId(1000L).createdBy("722cd920-e127-4cc2-93b9-e9b4a8f18873")
                .created(now()).amount(4d).minAmount(0.1d)
                .build()).block();
    }

    private void tearDownPositions() {
        mongoTemplate.remove(new Query(Criteria.where(MONGO_ID_FIELD_NAME).is("999")), Position.class).block();
        mongoTemplate.remove(new Query(Criteria.where(MONGO_ID_FIELD_NAME).is("1002")), Position.class).block();
        mongoTemplate.remove(new Query(Criteria.where(MONGO_ID_FIELD_NAME).is("12347")), Item.class).block();
    }
}
