package com.modsensoftware.marketplace.unit;

import com.modsensoftware.marketplace.CustomMongoContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author andrey.demyanchik on 12/20/2022
 */
@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class AbstractDaoTest {

    @Container
    public static CustomMongoContainer mongoContainer
            = CustomMongoContainer.getInstance();

    @Autowired
    protected ReactiveMongoTemplate mongoTemplate;
}
