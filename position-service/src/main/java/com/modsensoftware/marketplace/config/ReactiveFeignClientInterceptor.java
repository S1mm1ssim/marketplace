package com.modsensoftware.marketplace.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.client.ReactiveHttpRequestInterceptor;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.lang.String.format;

/**
 * @author andrey.demyanchik on 1/7/2023
 */
@Profile("!wiremock-test")
@Slf4j
@Component
public class ReactiveFeignClientInterceptor implements ReactiveHttpRequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer";
    @Value("${reactive-feign.oauth2.client.provider.position-service.token-uri}")
    private String providerUri;
    @Value("${reactive-feign.oauth2.client.registration.position-service.client-id}")
    private String clientId;
    @Value("${reactive-feign.oauth2.client.registration.position-service.client-secret}")
    private String clientSecret;
    @Value("${reactive-feign.oauth2.client.registration.position-service.authorization-grant-type}")
    private String authorizationGrantType;

    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<ReactiveHttpRequest> apply(ReactiveHttpRequest reactiveHttpRequest) {
        log.trace("Fetching access token for request from SSO");
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", authorizationGrantType);
        Mono<AccessTokenResponse> accessTokenResponseMono = webClient
                .method(HttpMethod.POST)
                .uri(providerUri)
                .bodyValue(formData)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .bodyToMono(AccessTokenResponse.class);
        return accessTokenResponseMono.flatMap(accessTokenResponse -> Mono.just(reactiveHttpRequest)
                .flatMap(request -> {
                    log.trace("Fetched access token successfully");
                    request.headers().put(AUTHORIZATION_HEADER, List.of(format("%s %s", BEARER, accessTokenResponse.getToken())));
                    return Mono.just(request);
                }));
    }
}
