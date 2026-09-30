package com.elms.common.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * STORY 03 - Processed Event Repository
 */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    /**
     * Check if event has already been processed by this consumer
     */
    Optional<ProcessedEvent> findByEventIdAndConsumer(String eventId, String consumer);

    /**
     * Check if event exists regardless of consumer
     */
    boolean existsByEventId(String eventId);
}

