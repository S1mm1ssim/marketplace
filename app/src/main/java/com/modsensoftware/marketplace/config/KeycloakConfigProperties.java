package com.modsensoftware.marketplace.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

/**
 * @author andrey.demyanchik on 12/28/2022
 */
@Getter
@ToString
@Setter
@Component
@ConfigurationProperties(prefix = "idm")
public class KeycloakConfigProperties {

    @NotBlank
    private String realmName;
    @NotBlank
    private String serverUrl;
    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    @NotBlank
    private String adminUsername;
    @NotBlank
    private String adminPassword;
    private int poolSize;
}
