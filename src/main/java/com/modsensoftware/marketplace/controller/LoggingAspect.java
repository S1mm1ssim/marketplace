package com.modsensoftware.marketplace.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author andrey.demyanchik on 12/13/2022
 */
@Slf4j
@Component
@Aspect
public class LoggingAspect {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Pointcut("within(com.modsensoftware.marketplace.controller..*) "
            + "&& @annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void getMappingPointcut() {
    }

    @Pointcut("within(com.modsensoftware.marketplace.controller..*) "
            + "&& @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMappingPointcut() {
    }

    @Pointcut("within(com.modsensoftware.marketplace.controller..*) "
            + "&& (@annotation(org.springframework.web.bind.annotation.PutMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void putAndDeleteMappingPointcut() {
    }

    @Before("getMappingPointcut()")
    public void logGetRequest(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GetMapping mapping = signature.getMethod().getAnnotation(GetMapping.class);

        Map<String, Object> parameters = getParameters(joinPoint);
        try {
            log.info("==> path(s): {}, method(s): {}, arguments: {} ",
                    mapping.path(), HttpMethod.GET, mapper.writeValueAsString(parameters));
        } catch (JsonProcessingException e) {
            log.error("Error while converting", e);
        }
    }

    @AfterReturning(pointcut = "getMappingPointcut()", returning = "entity")
    public void logGetResponse(JoinPoint joinPoint, Object entity) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        GetMapping mapping = signature.getMethod().getAnnotation(GetMapping.class);

        try {
            log.info("<== path(s): {}, method(s): {}, returning: {}",
                    mapping.path(), HttpMethod.GET, mapper.writeValueAsString(entity));
        } catch (JsonProcessingException e) {
            log.error("Error while converting", e);
        }
    }

    @Before("postMappingPointcut()")
    public void logPostRequest(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        PostMapping mapping = signature.getMethod().getAnnotation(PostMapping.class);

        Map<String, Object> parameters = getParameters(joinPoint);
        log.info("{}", joinPoint.getArgs());
        try {
            log.info("==> path(s): {}, method(s): {}, arguments: {} ",
                    mapping.path(), HttpMethod.POST, mapper.writeValueAsString(parameters));
        } catch (JsonProcessingException e) {
            log.error("Error while converting", e);
        }
    }

    @Before("putAndDeleteMappingPointcut()")
    public void logPutAndDeleteRequest(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        PutMapping putMapping = signature.getMethod().getAnnotation(PutMapping.class);
        DeleteMapping deleteMapping = signature.getMethod().getAnnotation(DeleteMapping.class);

        Map<String, Object> parameters = getParameters(joinPoint);
        log.info("{}", joinPoint.getArgs());
        try {
            if (putMapping != null) {
                log.info("==> path(s): {}, method(s): {}, arguments: {} ",
                        putMapping.path(), HttpMethod.PUT, mapper.writeValueAsString(parameters));
            } else {
                log.info("==> path(s): {}, method(s): {}, arguments: {} ",
                        deleteMapping.path(), HttpMethod.DELETE, mapper.writeValueAsString(parameters));
            }
        } catch (JsonProcessingException e) {
            log.error("Error while converting", e);
        }
    }

    @AfterReturning("putAndDeleteMappingPointcut()")
    public void logPutAndDeleteResponse(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        PutMapping putMapping = signature.getMethod().getAnnotation(PutMapping.class);
        DeleteMapping deleteMapping = signature.getMethod().getAnnotation(DeleteMapping.class);
        ResponseStatus responseStatus = signature.getMethod().getAnnotation(ResponseStatus.class);

        if (putMapping != null) {
            log.info("<== path(s): {}, method(s): {}",
                    putMapping.path(), HttpMethod.PUT);
        } else {
            log.info("<== path(s): {}, method(s): {}, response status: {} ",
                    deleteMapping.path(), HttpMethod.DELETE, responseStatus.value());
        }
    }


    private Map<String, Object> getParameters(JoinPoint joinPoint) {
        CodeSignature signature = (CodeSignature) joinPoint.getSignature();

        HashMap<String, Object> map = new HashMap<>();

        String[] parameterNames = signature.getParameterNames();

        for (int i = 0; i < parameterNames.length; i++) {
            map.put(parameterNames[i], joinPoint.getArgs()[i]);
        }

        return map;
    }
}
