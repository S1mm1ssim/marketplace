package com.modsensoftware.marketplace.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author andrey.demyanchik on 11/15/2022
 */
@Component
public class HibernateSessionFactory {

    private SessionFactory sessionFactory;

    @PostConstruct
    private void buildSessionFactory() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw new ExceptionInInitializerError("Initialization of SessionFactory failed" + e);
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
