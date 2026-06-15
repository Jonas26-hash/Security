package com.transporte.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final RedisTemplate<String, Integer> redisTemplate;
    private static final int RATE_LIMIT = 100; // solicitudes por minuto
    private static final long WINDOW = 60; // segundos

    public RateLimitingFilter(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            String userId = getUserId(exchange);
            String key = "rate_limit:" + userId;

            Integer currentRequests = redisTemplate.opsForValue().get(key);
            if (currentRequests == null) {
                currentRequests = 0;
                redisTemplate.opsForValue().set(key, 1, WINDOW, TimeUnit.SECONDS);
            } else if (currentRequests >= RATE_LIMIT) {
                log.warn("Rate limit exceeded for user: {}", userId);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            } else {
                redisTemplate.opsForValue().increment(key);
            }

            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("Error en rate limiting: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    private String getUserId(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).hashCode() + "";
        }
        return exchange.getRequest().getRemoteAddress().getHostName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
