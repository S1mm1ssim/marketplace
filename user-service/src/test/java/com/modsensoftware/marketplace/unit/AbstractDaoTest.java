package com.modsensoftware.marketplace.unit;

import com.modsensoftware.marketplace.CustomPostgreSQLContainer;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author andrey.demyanchik on 12/20/2022
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class AbstractDaoTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Container
    public static CustomPostgreSQLContainer postgreSQLContainer
            = CustomPostgreSQLContainer.getInstance();

    @Autowired
    protected SessionFactory sessionFactory;
}
