package com.elms.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * STORY 03 - Duplicate Event Handler
 * Prevents duplicate event processing (idempotent consumer pattern)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateEventHandler {

    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Check if event has already been processed by this consumer
     * @return true if event is duplicate, false if new
     */
    public boolean isDuplicate(String eventId, String consumer) {
        return processedEventRepository.findByEventIdAndConsumer(eventId, consumer).isPresent();
    }

    /**
     * Mark event as processed to prevent duplicate processing
     * Should be called AFTER successful event processing
     */
    @Transactional
    public void markAsProcessed(String eventId, String eventType, String aggregateId,
                               String consumer, String correlationId, Object payload) {
        try {
            ProcessedEvent processed = ProcessedEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .aggregateId(aggregateId)
                    .consumer(consumer)
                    .processedAt(LocalDateTime.now())
                    .correlationId(correlationId)
                    .payload(objectMapper.writeValueAsString(payload))
                    .build();

            processedEventRepository.save(processed);
            log.info("Marked event as processed: {} - {} by {}", eventId, eventType, consumer);
        } catch (Exception e) {
            log.error("Failed to mark event as processed", e);
            throw new RuntimeException("Failed to record processed event", e);
        }
    }

    /**
     * Check if event exists in the system (regardless of consumer)
     */
    public boolean eventExists(String eventId) {
        return processedEventRepository.existsByEventId(eventId);
    }
}

