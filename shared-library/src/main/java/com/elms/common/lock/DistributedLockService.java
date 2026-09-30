package com.elms.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * STORY 05 - Distributed Locking
 * Prevents concurrent modifications to shared resources
 * Uses Redis for distributed lock across multiple instances
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final long LOCK_TIMEOUT_SECONDS = 30;
    private static final long RETRY_DELAY_MS = 100;
    private static final int MAX_RETRIES = 50; // 5 seconds total

    /**
     * Acquire a distributed lock
     * @param resourceId The ID of the resource to lock
     * @return Lock token that must be used to release the lock
     */
    public String acquireLock(String resourceId) {
        String lockKey = LOCK_PREFIX + resourceId;
        String lockToken = generateLockToken();

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            Boolean lockAcquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, lockToken, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (lockAcquired != null && lockAcquired) {
                log.debug("Lock acquired for {}: {}", resourceId, lockToken);
                return lockToken;
            }

            attempts++;
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Lock acquisition interrupted", e);
            }
        }

        throw new RuntimeException("Failed to acquire lock for resource: " + resourceId);
    }

    /**
     * Release a distributed lock
     * @param resourceId The ID of the locked resource
     * @param lockToken The token returned from acquireLock
     */
    public void releaseLock(String resourceId, String lockToken) {
        String lockKey = LOCK_PREFIX + resourceId;

        // Only release if we hold the lock (prevent accidental unlock by wrong process)
        String currentToken = redisTemplate.opsForValue().get(lockKey);

        if (lockToken.equals(currentToken)) {
            redisTemplate.delete(lockKey);
            log.debug("Lock released for {}", resourceId);
        } else {
            log.warn("Lock token mismatch for {}. Current: {}, Provided: {}",
                    resourceId, currentToken, lockToken);
        }
    }

    /**
     * Execute a task with distributed lock
     * Lock is automatically acquired and released
     */
    public <T> T executeWithLock(String resourceId, LockableTask<T> task) {
        String lockToken = acquireLock(resourceId);
        try {
            return task.execute();
        } finally {
            releaseLock(resourceId, lockToken);
        }
    }

    /**
     * Check if a resource is currently locked
     */
    public boolean isLocked(String resourceId) {
        return redisTemplate.hasKey(LOCK_PREFIX + resourceId);
    }

    /**
     * Extend the expiration of an existing lock
     * Useful for long-running operations
     */
    public boolean extendLock(String resourceId, String lockToken) {
        String lockKey = LOCK_PREFIX + resourceId;
        String currentToken = redisTemplate.opsForValue().get(lockKey);

        if (lockToken.equals(currentToken)) {
            redisTemplate.expire(lockKey, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.debug("Lock extended for {}", resourceId);
            return true;
        }

        return false;
    }

    /**
     * Generate a unique lock token
     */
    private String generateLockToken() {
        return Thread.currentThread().getId() + "-" + System.nanoTime();
    }

    /**
     * Functional interface for tasks that require a lock
     */
    @FunctionalInterface
    public interface LockableTask<T> {
        T execute();
    }
}

