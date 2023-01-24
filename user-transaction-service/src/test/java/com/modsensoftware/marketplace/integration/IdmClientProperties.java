package com.modsensoftware.marketplace.integration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

/**
 * @author andrey.demyanchik on 1/24/2023
 */
@Getter
@ToString
@Setter
@Component
@ConfigurationProperties(prefix = "idm")
public class IdmClientProperties {

    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    @NotBlank
    private String grantType;

}
