package com.modsensoftware.marketplace.config;

import com.modsensoftware.marketplace.domain.Company;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andrey.demyanchik on 11/15/2022
 */
@Configuration
public class SessionFactoryConfig {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schemaName;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private String batchSize;

    @Value("${exception.message.sessionFactoryInitFail}")
    private String sessionFactoryInitFailMessage;

    @Bean
    public SessionFactory sessionFactory() {
        final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(hibernateProperties())
                .build();
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addAnnotatedClass(Company.class);
        Metadata metadata = metadataSources.buildMetadata();
        try {
            return metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
            throw new ExceptionInInitializerError(sessionFactoryInitFailMessage + e);
        }
    }

    private Map<String, String> hibernateProperties() {
        Map<String, String> settings = new HashMap<>();
        settings.put("connection.driver_class", driverClassName);
        settings.put("connection.url", url);
        settings.put("connection.username", username);
        settings.put("connection.password", password);
        settings.put("hibernate.default_schema", schemaName);
        settings.put("hibernate.jdbc.batch_size", batchSize);
        settings.put("dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        settings.put("hibernate.show_sql", "true");
        settings.put("hibernate.format_sql", "true");
        settings.put("hbm2ddl.auto", "none");
        settings.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        // For some reason HikariCP does not resolve its properties using connection.* properties specified above
        // Therefore setting hikari properties manually
        settings.put("hibernate.hikari.username", username);
        settings.put("hibernate.hikari.password", password);
        settings.put("hibernate.hikari.jdbcUrl", url);
        settings.put("hibernate.hikari.driverClassName", driverClassName);
        return settings;
    }
}
