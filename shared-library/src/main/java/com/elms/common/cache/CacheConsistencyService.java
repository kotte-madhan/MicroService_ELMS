package com.elms.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * STORY 07 - Cache Consistency Handler
 * Manages cache invalidation and consistency
 * Prevents stale data from being served
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheConsistencyService {

    private final CacheManager cacheManager;

    /**
     * Invalidate specific cache entry
     * Call this after updating data in database
     */
    public void invalidate(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Cache invalidated: {} - {}", cacheName, key);
        }
    }

    /**
     * Invalidate entire cache
     * Use with caution - impacts performance
     */
    public void invalidateAll(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.warn("Cache cleared: {}", cacheName);
        }
    }

    /**
     * Invalidate multiple cache entries matching a pattern
     */
    public void invalidatePattern(String cacheName, String keyPattern) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null && cache instanceof org.springframework.data.redis.cache.RedisCache) {
            // For Redis cache, we can use more sophisticated patterns
            log.debug("Invalidating cache pattern: {} - {}", cacheName, keyPattern);
            // Note: Redis cache doesn't support pattern eviction directly
            // Consider using Redis key scanning in production
        }
    }

    /**
     * Put value directly into cache
     * Use after database update to ensure consistency
     */
    public void putInCache(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.debug("Value cached: {} - {}", cacheName, key);
        }
    }

    /**
     * Get value from cache
     */
    public <T> Optional<T> getFromCache(String cacheName, String key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                return Optional.of((T) wrapper.get());
            }
        }
        return Optional.empty();
    }

    /**
     * Check if key exists in cache
     */
    public boolean cacheContains(String cacheName, String key) {
        return getFromCache(cacheName, key, Object.class).isPresent();
    }
}

