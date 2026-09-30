package com.elms.common.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * STORY 03 - Duplicate Event Handling
 * Tracks processed events to prevent duplicate processing
 * Idempotent event consumer pattern
 */
@Entity
@Table(name = "processed_events", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "consumer"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "consumer", nullable = false)
    private String consumer; // Service name that processed the event

    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload; // Store original payload for audit
}

