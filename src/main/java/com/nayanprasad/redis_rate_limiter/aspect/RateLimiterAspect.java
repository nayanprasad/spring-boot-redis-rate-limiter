package com.nayanprasad.redis_rate_limiter.aspect;

import com.nayanprasad.redis_rate_limiter.annotation.RateLimited;
import com.nayanprasad.redis_rate_limiter.exception.RateLimitExceededException;
import com.nayanprasad.redis_rate_limiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

@Aspect
@Component
@Order(1)
@Slf4j
@ConditionalOnProperty(name = "rate-limiter.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimiterAspect {

    private final RateLimiterService rateLimiterService;

    @Autowired
    public RateLimiterAspect(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Before("@annotation(com.nayanprasad.redis_rate_limiter.annotation.RateLimited)")
    public void rateLimit(JoinPoint joinPoint) throws RateLimitExceededException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String clientIp = request.getRemoteAddr();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimited rateLimitedAnnotation = method.getAnnotation(RateLimited.class);

        int limit = rateLimitedAnnotation.limit();
        int period = rateLimitedAnnotation.period();
        String key = rateLimitedAnnotation.key();

        if(key.isEmpty()) {
            key = method.getName();
        }

        String finalKey = key + ":" + clientIp;

        log.debug("Checking rate limit for key: {}, limit: {}, period: {}s", finalKey, limit, period);

        if(!rateLimiterService.allowRequest(finalKey, limit, period)) {
            log.warn("Rate limit exceeded for client: {} on method: {}", clientIp, method.getName());
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }

    }
}
