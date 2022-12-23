package com.modsensoftware.marketplace.integration;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author andrey.demyanchik on 11/22/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=8081",
                "management.server.port=9081"
        })
public abstract class AbstractIntegrationTest {

    private static final int DEFAULT_PORT = 8081;
    private static final String BASE_PATH = "/api/v1";

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    protected static final JdbcDatabaseDelegate dbDelegate
            = new JdbcDatabaseDelegate(postgreSQLContainer, "");

    @BeforeAll
    protected static void beforeAll() {
        String port = System.getProperty("server.port");
        if (port == null) {
            RestAssured.port = DEFAULT_PORT;
        } else {
            RestAssured.port = Integer.parseInt(port);
        }

        String basePath = System.getProperty("server.base");
        if (basePath == null) {
            basePath = BASE_PATH;
        }
        RestAssured.basePath = basePath;
    }
}