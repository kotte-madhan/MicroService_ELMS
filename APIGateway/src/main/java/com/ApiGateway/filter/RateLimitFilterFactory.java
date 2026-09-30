package com.ApiGateway.filter;

import com.elms.common.dto.ErrorResponse;
import com.elms.common.exception.RateLimitExceededException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Filter Factory for API Gateway (Story 14)
 * Implements rate limiting based on client IP/user
 * Default: 100 requests per minute per client
 */
@Slf4j
@Component
public class RateLimitFilterFactory extends AbstractGatewayFilterFactory<RateLimitFilterFactory.Config> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final int RATE_LIMIT_PER_MINUTE = 100;
    private static final long WINDOW_SIZE_SECONDS = 60;

    public RateLimitFilterFactory(RedisTemplate<String, Object> redisTemplate,
                                  ObjectMapper objectMapper) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = getClientIdentifier(exchange);
            String redisKey = "rate_limit:" + clientId;

            try {
                Long currentRequests = (Long) redisTemplate.opsForValue().get(redisKey);
                if (currentRequests == null) {
                    currentRequests = 0L;
                }

                if (currentRequests >= RATE_LIMIT_PER_MINUTE) {
                    log.warn("Rate limit exceeded for client: {}", clientId);
                    return handleRateLimitExceeded(exchange);
                }

                redisTemplate.opsForValue().increment(redisKey);
                redisTemplate.expire(redisKey, WINDOW_SIZE_SECONDS, TimeUnit.SECONDS);

                exchange.getAttributes().put("X-RateLimit-Remaining",
                        RATE_LIMIT_PER_MINUTE - currentRequests - 1);
                exchange.getAttributes().put("X-RateLimit-Reset",
                        System.currentTimeMillis() / 1000 + WINDOW_SIZE_SECONDS);

                return chain.filter(exchange);

            } catch (Exception ex) {
                log.error("Error in rate limiting filter", ex);
                return chain.filter(exchange);
            }
        };
    }

    /**
     * Handle rate limit exceeded response
     */
    private reactor.core.publisher.Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(429)
                .error("TOO_MANY_REQUESTS")
                .message("Rate limit exceeded. Maximum 100 requests per minute")
                .path(exchange.getRequest().getURI().getPath())
                .correlationId(getCorrelationId(exchange))
                .build();

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (IOException e) {
            log.error("Failed to serialize error response", e);
            bytes = "Rate limit exceeded".getBytes();
        }

        return exchange.getResponse().writeWith(
                reactor.core.publisher.Mono.just(
                        exchange.getResponse().bufferFactory().wrap(bytes)
                )
        );
    }

    /**
     * Get client identifier (IP address or user ID if authenticated)
     */
    private String getClientIdentifier(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        String clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress().getHostAddress();
        return "ip:" + clientIp;
    }

    /**
     * Get correlation ID from request
     */
    private String getCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        return correlationId != null ? correlationId : "REQ-" + System.nanoTime();
    }

    /**
     * Configuration class for the filter
     */
    public static class Config {
        private int requestsPerMinute = RATE_LIMIT_PER_MINUTE;

        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }

        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
    }
}

