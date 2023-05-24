package com.modsensoftware.marketplace.config.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author andrey.demyanchik on 1/23/2023
 */
@Configuration
@EnableCaching
public class ReactiveRedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;

    @Primary
    @Bean
    public ReactiveRedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> valueSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        valueSerializer.setObjectMapper(new ObjectMapper().findAndRegisterModules());
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializationContext<String, Object> serializationContext
                = RedisSerializationContext.<String, Object>newSerializationContext()
                .key(keySerializer).hashKey(keySerializer)
                .value(valueSerializer).hashValue(valueSerializer)
                .build();
        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }
}
