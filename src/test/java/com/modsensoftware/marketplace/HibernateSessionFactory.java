package com.modsensoftware.marketplace;

import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Company;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.domain.Position;
import com.modsensoftware.marketplace.domain.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andrey.demyanchik on 11/21/2022
 */
public class HibernateSessionFactory {

    private SessionFactory sessionFactory;

    private SessionFactory createSessionFactory() {
        Map<String, String> settings = new HashMap<>();
        settings.put("connection.driver_class", "org.postgresql.Driver");
        settings.put("connection.url", ContainerInitializer.postgreSQLContainer.getJdbcUrl());
        settings.put("hibernate.connection.username", "postgres");
        settings.put("hibernate.connection.password", "postgres");
        settings.put("dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        settings.put("hibernate.show_sql", "true");
        settings.put("hibernate.format_sql", "true");
        settings.put("hbm2ddl.auto", "none");
        settings.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings).build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addAnnotatedClass(Category.class);
        metadataSources.addAnnotatedClass(User.class);
        metadataSources.addAnnotatedClass(Company.class);
        metadataSources.addAnnotatedClass(Item.class);
        metadataSources.addAnnotatedClass(Position.class);
        Metadata metadata = metadataSources.buildMetadata();
        return metadata.getSessionFactoryBuilder().build();
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = createSessionFactory();
        }
        return sessionFactory;
    }
}
