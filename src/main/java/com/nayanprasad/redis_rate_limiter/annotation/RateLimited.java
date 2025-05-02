package com.nayanprasad.redis_rate_limiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    int capacity() default 5; // Maximum number of requests allowed within the specified time period

    int timeWindowSeconds() default 60; // Time period in seconds for the rate limit window

    String key() default ""; // Key to identify the rate limit. Defaults to the method name.
}
