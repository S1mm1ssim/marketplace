package com.modsensoftware.marketplace.config.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author andrey.demyanchik on 12/20/2022
 */
@Slf4j
public class OAuthClientCredentialsFeignManager {

    private final OAuth2AuthorizedClientManager manager;
    private final Authentication principal;
    private final ClientRegistration clientRegistration;

    private static final String COULD_NOT_AUTHORIZE_CLIENT_EXCEPTION_MESSAGE
            = "Client credentials flow on %s failed, client is null";

    public OAuthClientCredentialsFeignManager(OAuth2AuthorizedClientManager manager,
                                              ClientRegistration clientRegistration) {
        this.manager = manager;
        this.principal = createPrincipal();
        this.clientRegistration = clientRegistration;
    }

    private Authentication createPrincipal() {
        return new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.emptySet();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return this;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }

            @Override
            public String getName() {
                return clientRegistration.getClientId();
            }
        };
    }

    public String getAccessToken() {
        try {
            OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(clientRegistration.getRegistrationId())
                    .principal(principal)
                    .build();
            OAuth2AuthorizedClient client = manager.authorize(oAuth2AuthorizeRequest);
            if (Objects.isNull(client)) {
                throw new IllegalStateException(String.format(COULD_NOT_AUTHORIZE_CLIENT_EXCEPTION_MESSAGE,
                        clientRegistration.getRegistrationId()));
            }
            return client.getAccessToken().getTokenValue();
        } catch (Exception e) {
            log.error("Client credentials error " + e.getMessage());
        }
        return null;
    }
}
