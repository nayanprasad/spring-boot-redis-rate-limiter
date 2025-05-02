package com.nayanprasad.redis_rate_limiter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateLimiterService {
    private final RedisTemplate<String, Long> redisTemplate;

    @Autowired
    public RateLimiterService(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

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
}
