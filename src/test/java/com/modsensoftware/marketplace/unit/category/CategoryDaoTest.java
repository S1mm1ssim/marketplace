package com.modsensoftware.marketplace.unit.category;

import com.modsensoftware.marketplace.ContainerInitializer;
import com.modsensoftware.marketplace.HibernateSessionFactory;
import com.modsensoftware.marketplace.dao.CategoryDao;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author andrey.demyanchik on 11/21/2022
 */
@SpringBootTest
@ContextConfiguration(classes = {ContainerInitializer.class})
public class CategoryDaoTest {

    private CategoryDao underTest;

    private SessionFactory sessionFactory;

    private final HibernateSessionFactory hibernateSessionFactory = new HibernateSessionFactory();

    @BeforeEach
    void setUp() {
        sessionFactory = hibernateSessionFactory.getSessionFactory();
        ReflectionTestUtils.setField(underTest, "sessionFactory", sessionFactory);
    }

    @Test
    public void canGetById() {
        System.out.println(ContainerInitializer.postgreSQLContainer.getJdbcUrl());
    }
}
