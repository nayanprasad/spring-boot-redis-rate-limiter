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

    @Value("${rate.limit.capacity:10}")
    private int capacity;

    @Value("${rate.limit.time-window-seconds:60}")
    private int timeWindowSeconds;

    @Autowired
    public RateLimiterService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // This method uses Redis Sorted Sets (ZSets) to implement a sliding window rate limiter:
    public boolean allowRequest(String key, int limit, int period) {
        String redisKey = "rate:limit:" + key;
        long now = Instant.now().getEpochSecond();

        Long removed = redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, now - period);
        if(removed != null) {
            log.debug("Removed {} expired entries for key: {}", removed, redisKey);
        }

        Long count = redisTemplate.opsForZSet().zCard(redisKey);
        log.debug("Current request count for {}: {}/{}", key, count, limit);

        if(count != null && count >= limit) {
            log.warn("Rate limit exceeded for key: {}", key);
            return false;
        }

        redisTemplate.opsForZSet().add(redisKey, now, now);

        // Set expiration on the key to auto-cleanup
        redisTemplate.expire(redisKey, period + 5, TimeUnit.SECONDS);

        return true;
    }

    // This method uses Redis simple key-value pairs with a fixed counter approach:
    public boolean allowRequest(String key) {
        String redisKey = "rate_limit:" + key;

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
