package com.elms.common.saga;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * STORY 04 - Saga Step Execution
 * Tracks individual step execution within a saga
 */
@Entity
@Table(name = "saga_step_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaStepExecution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "saga_id", referencedColumnName = "id")
    private SagaExecution sagaExecution;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "service_name", nullable = false)
    private String serviceName; // e.g., employee-service, leave-service

    @Column(name = "action", nullable = false)
    private String action; // e.g., VALIDATE_EMPLOYEE, UPDATE_BALANCE

    @Column(name = "compensation_action")
    private String compensationAction; // e.g., RESTORE_BALANCE

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData; // JSON response from service

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired; // If false, failure doesn't fail the saga

    /**
     * Step Execution Status
     */
    public enum StepStatus {
        PENDING,       // Awaiting execution
        EXECUTING,     // Currently executing
        SUCCESS,       // Step completed successfully
        FAILED,        // Step failed
        COMPENSATING,  // Compensation in progress
        COMPENSATED,   // Compensation completed
        SKIPPED        // Step was skipped
    }
}

