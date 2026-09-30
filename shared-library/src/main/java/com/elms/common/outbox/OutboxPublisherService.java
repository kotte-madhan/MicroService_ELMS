package com.elms.common.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * STORY 01 - Outbox Publisher Service
 * Publishes events from Outbox table to RabbitMQ
 * Ensures no event is lost even if RabbitMQ temporarily fails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final String EXCHANGE_NAME = "elms.events";
    private static final int MAX_RETRIES = 3;

    /**
     * Save an event to Outbox (atomic with business transaction)
     * Should be called within @Transactional boundary
     */
    @Transactional
    public void saveEvent(String eventType, String aggregateId, String aggregateType,
                         Object payload, String correlationId) {
        try {
            OutboxEvent event = OutboxEvent.builder()
                    .eventType(eventType)
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .payload(objectMapper.writeValueAsString(payload))
                    .status(OutboxEvent.OutboxStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .maxRetries(MAX_RETRIES)
                    .correlationId(correlationId)
                    .build();

            outboxEventRepository.save(event);
            log.info("Outbox event saved: {} - {}", eventType, aggregateId);
        } catch (Exception e) {
            log.error("Failed to save outbox event", e);
            throw new RuntimeException("Outbox event save failed", e);
        }
    }

    /**
     * Periodically process pending outbox events
     * Scheduled to run every 5 seconds
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxEvent.OutboxStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            publishEvent(event);
        }
    }

    /**
     * Retry failed events that haven't exceeded max retries
     * Scheduled to run every 30 seconds
     */
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retryFailedEvents() {
        List<OutboxEvent> retryableEvents = outboxEventRepository.findRetryableEvents();

        for (OutboxEvent event : retryableEvents) {
            publishEvent(event);
        }
    }

    /**
     * Mark permanently failed events (exceeded retries) as dead lettered
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void markDeadLetteredEvents() {
        List<OutboxEvent> failedEvents = outboxEventRepository.findFailedEvents();

        for (OutboxEvent event : failedEvents) {
            event.setStatus(OutboxEvent.OutboxStatus.DEAD_LETTERED);
            outboxEventRepository.save(event);
            log.warn("Event moved to DLQ: {} - {} (retries: {})",
                    event.getEventType(), event.getAggregateId(), event.getRetryCount());
        }
    }

    /**
     * Publish single event to RabbitMQ
     */
    private void publishEvent(OutboxEvent event) {
        try {
            event.setStatus(OutboxEvent.OutboxStatus.PROCESSING);
            outboxEventRepository.save(event);

            String routingKey = event.getEventType().toLowerCase();
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, event.getPayload());

            event.setStatus(OutboxEvent.OutboxStatus.PUBLISHED);
            event.setProcessedAt(LocalDateTime.now());
            outboxEventRepository.save(event);

            log.info("Outbox event published: {} - {}", event.getEventType(), event.getAggregateId());
        } catch (Exception e) {
            handlePublishFailure(event, e);
        }
    }

    /**
     * Handle publication failure with retry logic
     */
    private void handlePublishFailure(OutboxEvent event, Exception e) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(e.getMessage());

        if (event.getRetryCount() >= event.getMaxRetries()) {
            event.setStatus(OutboxEvent.OutboxStatus.FAILED);
            log.error("Outbox event publishing failed after {} retries: {} - {}",
                    event.getMaxRetries(), event.getEventType(), event.getAggregateId(), e);
        } else {
            event.setStatus(OutboxEvent.OutboxStatus.PENDING);
            log.warn("Outbox event publish failed, will retry: {} - {} (attempt {}/{})",
                    event.getEventType(), event.getAggregateId(),
                    event.getRetryCount(), event.getMaxRetries());
        }

        outboxEventRepository.save(event);
    }

    /**
     * Clean up old published events (older than 24 hours)
     * Can be called periodically to reduce table size
     */
    @Scheduled(cron = "0 0 2 * * *") // 2 AM daily
    @Transactional
    public void cleanupOldPublishedEvents() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<OutboxEvent> oldEvents = outboxEventRepository.findByStatusAndProcessedAtBefore(
                OutboxEvent.OutboxStatus.PUBLISHED, cutoffTime);

        outboxEventRepository.deleteAll(oldEvents);
        log.info("Cleaned up {} old published events", oldEvents.size());
    }
}

