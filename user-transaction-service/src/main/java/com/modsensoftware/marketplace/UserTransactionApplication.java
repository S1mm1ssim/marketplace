package com.modsensoftware.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author andrey.demyanchik on 12/26/2022
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class UserTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserTransactionApplication.class, args);
    }
}
