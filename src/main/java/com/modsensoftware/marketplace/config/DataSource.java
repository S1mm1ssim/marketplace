package com.modsensoftware.marketplace.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author andrey.demyanchik on 11/1/2022
 */
public class DataSource {

    private static final HikariConfig CONFIG = new HikariConfig();
    private static final HikariDataSource DATA_SOURCE;

    static {
        CONFIG.setJdbcUrl("jdbc:postgresql://localhost:32768/marketplace");
        CONFIG.setUsername("postgres");
        CONFIG.setPassword("postgres");
        DATA_SOURCE = new HikariDataSource(CONFIG);
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }
}
