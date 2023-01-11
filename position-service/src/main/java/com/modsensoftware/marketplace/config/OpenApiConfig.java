package com.modsensoftware.marketplace.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * @author andrey.demyanchik on 11/24/2022
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Marketplace API",
                description = "A marketplace where companies can place positions and users can buy them.",
                termsOfService = "Terms of service.",
                contact = @Contact(
                        name = "Andrey Demyanchik", email = "Andrdemyan4ik@gmail.com"
                ),
                version = "v1"
        )
)
public class OpenApiConfig {
}
