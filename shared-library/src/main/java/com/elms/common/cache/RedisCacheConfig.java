package com.elms.common.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * STORY 06 & 07 - Advanced Redis Caching Configuration
 * Enables Spring Cache with Redis backend
 * Supports @Cacheable, @CachePut, @CacheEvict annotations
 *
 * Note: Redis serialization is auto-configured by Spring Boot
 * when spring.cache.type=redis in application.properties
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheConfig {

    // Redis cache manager is auto-configured by Spring Boot
    // No additional configuration needed
}


