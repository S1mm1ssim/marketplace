package com.modsensoftware.marketplace;

import com.modsensoftware.marketplace.service.impl.CompanyClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author andrey.demyanchik on 1/3/2023
 */
@SpringBootApplication
@EnableEurekaClient
@EnableCaching
@EnableFeignClients(clients = CompanyClient.class)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
