package com.nayanprasad.redis_rate_limiter.config;

import com.nayanprasad.redis_rate_limiter.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
//                .addPathPatterns("/hello")  // Apply to /hello path
                .excludePathPatterns("/error", "/hello");  // Exclude error paths
    }
}
