package com.nayanprasad.redis_rate_limiter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateLimiterService {
    private final RedisTemplate<String, Object> redisTemplate;


    @Autowired
    public RateLimiterService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // This method uses Redis simple key-value pairs with a fixed counter approach:
    public boolean allowRequest(String key, int capacity, int timeWindowSeconds) {
        String redisKey = "rate:limit:" + key;

        // Check if key exists
        Boolean keyExists = redisTemplate.hasKey(redisKey);

        if (keyExists == null || !keyExists) {
            // First request, initialize counter
            redisTemplate.opsForValue().set(redisKey, 1, Duration.ofSeconds(timeWindowSeconds));
            return true;
        }

        // Increment counter
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            // Something went wrong with Redis, allow by default but log error
            log.error("Failed to increment rate limit counter for client: {}", key);
            return true;
        }

        return currentCount <= capacity;
    }

}
