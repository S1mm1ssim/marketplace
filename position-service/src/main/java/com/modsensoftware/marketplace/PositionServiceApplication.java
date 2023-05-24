package com.modsensoftware.marketplace;

import com.modsensoftware.marketplace.service.impl.UserClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableReactiveFeignClients(clients = {UserClient.class})
@EnableCaching
@EnableReactiveMongoAuditing
public class PositionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PositionServiceApplication.class, args);
    }

}
