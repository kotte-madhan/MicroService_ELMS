package com.elms.common.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * STORY 01 - Outbox Repository
 * Manages Outbox events for reliable event publishing
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Find pending events that need to be published
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus status);

    /**
     * Find events pending with retry count < max retries
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' AND o.retryCount < o.maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findRetryableEvents();

    /**
     * Find failed events that have exceeded max retries
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' ORDER BY o.createdAt ASC")
    List<OutboxEvent> findFailedEvents();

    /**
     * Find recently published events (for cleanup/archival)
     */
    List<OutboxEvent> findByStatusAndProcessedAtBefore(OutboxEvent.OutboxStatus status, LocalDateTime before);

    /**
     * Find event by correlation ID
     */
    Optional<OutboxEvent> findByCorrelationId(String correlationId);

    /**
     * Find events by aggregate ID
     */
    List<OutboxEvent> findByAggregateId(String aggregateId);
}

