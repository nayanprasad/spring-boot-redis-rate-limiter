package com.nayanprasad.redis_rate_limiter.controller;

import com.nayanprasad.redis_rate_limiter.exception.RateLimitExceededException;
import com.nayanprasad.redis_rate_limiter.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse>  handleRateLimitExceededException(RateLimitExceededException rateLimitExceededException) {
        return new ResponseEntity<>(new ApiResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                rateLimitExceededException.getMessage(),
                System.currentTimeMillis()), HttpStatus.TOO_MANY_REQUESTS);
    }
}
