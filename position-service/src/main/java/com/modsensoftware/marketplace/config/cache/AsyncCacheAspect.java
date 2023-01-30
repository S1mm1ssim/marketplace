package com.modsensoftware.marketplace.config.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsensoftware.marketplace.domain.Category;
import com.modsensoftware.marketplace.domain.Item;
import com.modsensoftware.marketplace.dto.response.PositionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.modsensoftware.marketplace.constants.Constants.CATEGORIES_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.ITEMS_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.POSITIONS_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.SINGLE_CATEGORY_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.SINGLE_ITEM_CACHE_NAME;
import static com.modsensoftware.marketplace.constants.Constants.SINGLE_POSITION_CACHE_NAME;

/**
 * @author andrey.demyanchik on 1/25/2023
 */
@Aspect
@Slf4j
@Component
public class AsyncCacheAspect {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final Map<Class<?>, Integer> classTtlMap = new HashMap<>();
    private final Map<String, TypeReference<?>> cacheNameTypeRefMap = new HashMap<>();

    private static final String CACHE_KEY_DELIMITER = "::";
    private static final String KEY_PATTERN = "#p";
    private static final String CACHE_NAME_SPLIT_REGEX = "::.*";
    private static final String DEFAULT_TEMP_VALUE_IF_CACHE_NOT_FOUND = "cacheNotFound";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    public AsyncCacheAspect(ReactiveRedisTemplate<String, Object> redisTemplate,
                            @Value("${cache.category.ttl-seconds}") int categoryTtlSeconds,
                            @Value("${cache.item.ttl-seconds}") int itemTtlSeconds,
                            @Value("${cache.position.ttl-seconds}") int positionTtlSeconds) {
        this.redisTemplate = redisTemplate;

        classTtlMap.put(Category.class, categoryTtlSeconds);
        classTtlMap.put(Item.class, itemTtlSeconds);
        classTtlMap.put(PositionResponseDto.class, positionTtlSeconds);

        cacheNameTypeRefMap.put(SINGLE_POSITION_CACHE_NAME, new TypeReference<PositionResponseDto>() {
        });
        cacheNameTypeRefMap.put(POSITIONS_CACHE_NAME, new TypeReference<List<PositionResponseDto>>() {
        });
        cacheNameTypeRefMap.put(SINGLE_CATEGORY_CACHE_NAME, new TypeReference<Category>() {
        });
        cacheNameTypeRefMap.put(CATEGORIES_CACHE_NAME, new TypeReference<List<Category>>() {
        });
        cacheNameTypeRefMap.put(SINGLE_ITEM_CACHE_NAME, new TypeReference<Item>() {
        });
        cacheNameTypeRefMap.put(ITEMS_CACHE_NAME, new TypeReference<List<Item>>() {
        });
    }

    @Pointcut("@annotation(AsyncCacheable)")
    public void pointcutCacheable() {

    }

    @Around("pointcutCacheable()")
    public Object cacheable(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
        Type rawType = parameterizedType.getRawType();

        if (!rawType.equals(Mono.class) && !rawType.equals(Flux.class)) {
            throw new IllegalArgumentException("The return type is not Mono/Flux. "
                    + "Use Mono/Flux for return type. method: " + method.getName());
        }
        AsyncCacheable asyncCacheable = method.getAnnotation(AsyncCacheable.class);
        String cacheName = asyncCacheable.cacheName();
        if (cacheName.isEmpty()) {
            throw new IllegalArgumentException("Cache name cannot be empty. Method: " + method.getName());
        }

        String redisKey = evaluateKey(asyncCacheable, joinPoint, cacheName);
        Mono<Object> mono = redisTemplate.opsForValue().get(redisKey)
                .defaultIfEmpty(DEFAULT_TEMP_VALUE_IF_CACHE_NOT_FOUND);
        // In case method return type is Mono
        if (rawType.equals(Mono.class)) {
            return mono.flatMap(value -> {
                if (value.toString().equals(DEFAULT_TEMP_VALUE_IF_CACHE_NOT_FOUND)) {
                    try {
                        log.debug("Could not find cached value. Will proceed with execution and try to cache the result");
                        return ((Mono<?>) joinPoint.proceed())
                                .map(result -> {
                                    cacheValue(redisKey, result);
                                    return result;
                                });
                    } catch (Throwable cause) {
                        return Mono.error(new RuntimeException("Something went wrong "
                                + "while attempting to proceed method execution", cause));
                    }
                }
                log.debug("Found cached value");
                return Mono.just(OBJECT_MAPPER.convertValue(value,
                        cacheNameTypeRefMap.get(redisKey.split(CACHE_NAME_SPLIT_REGEX)[0])));
            });
        }
        // In case method return type is Flux
        return Flux.from(mono).flatMap(values -> {
            if (values.toString().equals(DEFAULT_TEMP_VALUE_IF_CACHE_NOT_FOUND)) {
                try {
                    log.debug("Could not find cached value. Will proceed with execution and try to cache the result");
                    return ((Flux<?>) joinPoint.proceed())
                            .collectList()
                            .map(result -> {
                                cacheValue(redisKey, result);
                                return result;
                            }).flatMapMany(Flux::fromIterable);
                } catch (Throwable cause) {
                    return Flux.error(new RuntimeException("Something went wrong "
                            + "while attempting to proceed method execution", cause));
                }
            }
            log.debug("Found cached value");
            return Flux.fromIterable((Iterable<?>) OBJECT_MAPPER.convertValue(values,
                    cacheNameTypeRefMap.get(redisKey.split(CACHE_NAME_SPLIT_REGEX)[0])));
        });
    }

    private void cacheValue(String key, Object value) {
        Integer cacheTtl;
        if (value instanceof List) {
            cacheTtl = classTtlMap.get(((List<?>) value).get(0).getClass());
        } else {
            cacheTtl = classTtlMap.get(value.getClass());
        }
        if (cacheTtl != null) {
            redisTemplate.opsForValue().set(key, value).map(isSuccess -> {
                if (isSuccess) {
                    return redisTemplate.expire(key, Duration.ofSeconds(cacheTtl)).subscribe();
                } else {
                    log.error("Could not cache result of the operation");
                    return Mono.just(Boolean.FALSE);
                }
            }).subscribe();
            log.info("Result cached");
        } else {
            log.error("Could not cache value of class {} as no ttl "
                    + "is provided for instances of type", value.getClass());
        }
    }

    private String evaluateKey(AsyncCacheable asyncCacheable,
                               ProceedingJoinPoint joinPoint,
                               String cacheName) {
        String key = asyncCacheable.key();
        StringBuilder keyValue = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        if (key.isEmpty()) {
            for (Object arg : args) {
                keyValue.append(arg);
            }
        } else {
            int spelParamIndex = Integer.parseInt(key.replaceAll(KEY_PATTERN, ""));
            keyValue.append(args[spelParamIndex - 1].toString());
        }
        return cacheName + CACHE_KEY_DELIMITER + keyValue;
    }
}
