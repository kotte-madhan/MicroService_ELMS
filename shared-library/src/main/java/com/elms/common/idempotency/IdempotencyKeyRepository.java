package com.elms.common.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for managing idempotency keys
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

    /**
     * Find an idempotency key that hasn't expired
     */
    Optional<IdempotencyKey> findByIdempotencyKeyAndExpiresAtAfter(
            String idempotencyKey,
            LocalDateTime now);

    /**
     * Delete expired idempotency keys
     */
    void deleteByExpiresAtBefore(LocalDateTime now);
}

