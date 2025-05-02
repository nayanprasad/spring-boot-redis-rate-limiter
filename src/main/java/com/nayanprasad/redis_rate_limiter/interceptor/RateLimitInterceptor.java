package com.nayanprasad.redis_rate_limiter.interceptor;

import com.nayanprasad.redis_rate_limiter.exception.RateLimitExceededException;
import com.nayanprasad.redis_rate_limiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimiterService rateLimiterService;

    @Value("${rate.limit.capacity:10}")
    private int capacity;

    @Value("${rate.limit.time-window-seconds:60}")
    private int timeWindowSeconds;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientIdentifier(request);
        boolean isAllowed = rateLimiterService.allowRequest(clientId, capacity, timeWindowSeconds);

        if(!isAllowed) {
            log.warn("Rate limit exceeded for client: {}", clientId);
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }

        return  true;
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // In production, you might use a combination of IP, user ID (if authenticated), etc.
        return request.getRemoteAddr();
    }
}
