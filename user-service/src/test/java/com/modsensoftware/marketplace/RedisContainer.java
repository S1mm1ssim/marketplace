package com.modsensoftware.marketplace;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

/**
 * @author andrey.demyanchik on 1/23/2023
 */
@Slf4j
public class RedisContainer extends GenericContainer<RedisContainer> {

    private static final String REDIS_IMAGE = "redis:latest";
    private static final int REDIS_PORT = 6379;
    private static RedisContainer redisContainer;

    private RedisContainer() {
        super(REDIS_IMAGE);
        super.withExposedPorts(REDIS_PORT);
    }

    public static synchronized RedisContainer getInstance() {
        if (redisContainer == null) {
            redisContainer = new RedisContainer();
        }
        return redisContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("spring.redis.host", redisContainer.getHost());
        System.setProperty("spring.redis.port", redisContainer.getMappedPort(REDIS_PORT).toString());
        log.info("Started Redis container on port {}", redisContainer.getMappedPort(REDIS_PORT).toString());
    }

    @Override
    public void stop() {
    }
}
