package com.modsensoftware.marketplace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsensoftware.marketplace.dto.CompanyResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;

import static com.modsensoftware.marketplace.constants.Constants.COMPANIES_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.SINGLE_COMPANY_CACHE_NAME;

/**
 * @author andrey.demyanchik on 1/23/2023
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;
    @Value("${cache.company.ttl-seconds}")
    private int companyTtlSeconds;

    @Bean
    public RedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        Jackson2JsonRedisSerializer<CompanyResponseDto> companyValueSerializer
                = new Jackson2JsonRedisSerializer<>(CompanyResponseDto.class);
        companyValueSerializer.setObjectMapper(mapper);
        Jackson2JsonRedisSerializer<List> companiesValueSerializer
                = new Jackson2JsonRedisSerializer<>(List.class);
        companiesValueSerializer.setObjectMapper(mapper);
        return builder -> builder
                .withCacheConfiguration(SINGLE_COMPANY_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(companyTtlSeconds))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(companyValueSerializer)))
                .withCacheConfiguration(COMPANIES_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(companyTtlSeconds))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(companiesValueSerializer)));
    }
}
