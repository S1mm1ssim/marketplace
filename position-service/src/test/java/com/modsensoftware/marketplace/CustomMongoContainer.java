package com.modsensoftware.marketplace;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.MongoDBContainer;

/**
 * @author andrey.demyanchik on 1/4/2023
 */
@Slf4j
public class CustomMongoContainer extends MongoDBContainer {

    private static final String MONGO_IMAGE = "mongo:latest";

    private static CustomMongoContainer instance;

    private CustomMongoContainer() {
        super(MONGO_IMAGE);
    }

    public static synchronized CustomMongoContainer getInstance() {
        if (instance == null) {
            instance = new CustomMongoContainer();
        }
        return instance;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("spring.data.mongodb.uri", instance.getReplicaSetUrl());
        log.info("Started mongodb container. Connection uri {}", instance.getReplicaSetUrl());
    }

    @Override
    public void stop() {

    }
}
