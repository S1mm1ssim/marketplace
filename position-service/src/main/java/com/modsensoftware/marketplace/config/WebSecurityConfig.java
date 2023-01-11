package com.modsensoftware.marketplace.config;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author andrey.demyanchik on 11/30/2022
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Value("${default.role-prefix}")
    private String rolePrefix;
    @Value("${default.jwt-realm-access-claim}")
    private String realmAccessClaimName;
    @Value("${default.jwt-roles-claim}")
    private String rolesClaimName;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
                .httpBasic().disable()
                .csrf().disable()
                .authorizeExchange(auth -> auth
                        .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/webjars/**", "/v3/api-docs/**",
                                "/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt()
                        .jwtAuthenticationConverter(customJwtAuthenticationConverter())
                );
        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter customJwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }

    @Bean
    public Converter<Jwt, Flux<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();

        return new Converter<>() {
            @Override
            public Flux<GrantedAuthority> convert(Jwt jwt) {
                Collection<GrantedAuthority> grantedAuthorities = delegate.convert(jwt);

                if (jwt.getClaim(realmAccessClaimName) == null) {
                    return Flux.fromIterable(grantedAuthorities);
                }
                JSONObject realmAccess = jwt.getClaim(realmAccessClaimName);
                if (realmAccess.get(rolesClaimName) == null) {
                    return Flux.fromIterable(grantedAuthorities);
                }
                JSONArray roles = (JSONArray) realmAccess.get(rolesClaimName);
                final List<SimpleGrantedAuthority> keycloakAuthorities = roles
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(rolePrefix + role))
                        .collect(Collectors.toList());
                grantedAuthorities.addAll(keycloakAuthorities);
                return Flux.fromIterable(grantedAuthorities);
            }
        };
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri);
    }
}
