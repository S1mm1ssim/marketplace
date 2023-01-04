package com.modsensoftware.marketplace.unit;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * This container configuration is reusable between different tests
 *
 * @author andrey.demyanchik on 11/21/2022
 */
@Slf4j
public class CustomPostgreSQLContainer extends PostgreSQLContainer<CustomPostgreSQLContainer> {

    private static final String IMAGE_VERSION = "postgres:latest";
    private static CustomPostgreSQLContainer postgreSQLContainer;

    private CustomPostgreSQLContainer() {
        super(IMAGE_VERSION);
        super.withDatabaseName("marketplace_tests")
                .withUsername("postgres")
                .withPassword("postgres")
                .withInitScript("schema.sql");
    }

    public static synchronized CustomPostgreSQLContainer getInstance() {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = new CustomPostgreSQLContainer();
        }
        return postgreSQLContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
        log.info("Started PostgreSQL container; URL [{}] with USERNAME [{}] and with PASSWORD [{}]",
                postgreSQLContainer.getJdbcUrl(),
                postgreSQLContainer.getUsername(),
                postgreSQLContainer.getPassword());
    }

    @Override
    public void stop() {
    }
}
