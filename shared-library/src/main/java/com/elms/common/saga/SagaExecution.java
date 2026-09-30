package com.elms.common.saga;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * STORY 04 - Advanced Saga Implementation
 * Tracks distributed transaction state across multiple services
 */
@Entity
@Table(name = "saga_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaExecution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_id", unique = true, nullable = false)
    private String sagaId;

    @Column(name = "saga_type", nullable = false)
    private String sagaType; // e.g., LEAVE_APPROVAL_SAGA

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId; // Business entity ID (leave request ID)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep;

    @Column(name = "total_steps", nullable = false)
    private Integer totalSteps;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload; // JSON request data

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "compensation_started_at")
    private LocalDateTime compensationStartedAt;

    @Column(name = "compensation_completed_at")
    private LocalDateTime compensationCompletedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "correlation_id")
    private String correlationId;

    /**
     * Saga Execution Status
     */
    public enum SagaStatus {
        STARTED,           // Saga just created
        EMPLOYEE_VALIDATED, // Employee details validated
        BALANCE_CHECKED,   // Leave balance verified
        LEAVE_CREATED,     // Leave request created
        BALANCE_UPDATED,   // Leave balance deducted
        EVENT_PUBLISHED,   // Event published to RabbitMQ
        COMPLETED,         // Saga completed successfully

        FAILED,            // Saga failed at some step
        COMPENSATING,      // Compensation in progress
        COMPENSATED,       // Compensation completed
        FAILED_PERMANENTLY // Compensation also failed
    }
}

