package com.nayanprasad.redis_rate_limiter.controller;

import com.nayanprasad.redis_rate_limiter.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse> hello() {
        log.info("Hello endpoint called");
        return new ResponseEntity<>(new ApiResponse(HttpStatus.OK.value(), "Hello world", System.currentTimeMillis()), HttpStatus.OK);
    }
}
