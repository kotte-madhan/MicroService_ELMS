package com.LeaveService.LeaveService.Config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Simple ConcurrentMapCacheManager for local/dev. Replace with Redis/Caffeine in production.
        return new ConcurrentMapCacheManager();
    }
}

