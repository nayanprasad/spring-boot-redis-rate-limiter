package com.nayanprasad.redis_rate_limiter.exception;

public class RateLimitExceededException extends Exception{
    public RateLimitExceededException(String message) {
        super(message);
    }
}
