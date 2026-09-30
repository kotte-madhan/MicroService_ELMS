package com.elms.common.saga;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Saga Definition for distributed transactions
 * Defines the steps and compensation actions for a multi-service operation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaDefinition implements Serializable {

    @JsonProperty("saga_id")
    private String sagaId;

    @JsonProperty("saga_type")
    private String sagaType; // e.g., LEAVE_APPROVAL_SAGA

    @JsonProperty("status")
    private SagaStatus status;

    @JsonProperty("current_step")
    private Integer currentStep;

    @JsonProperty("aggregate_id")
    private String aggregateId; // The business entity ID (leave request ID, etc.)

    @JsonProperty("payload")
    private String payload; // JSON serialized request data

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("retry_count")
    private Integer retryCount;

    @JsonProperty("max_retries")
    private Integer maxRetries;

    /**
     * Saga Status Enum
     */
    public enum SagaStatus {
        STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        COMPENSATING,
        COMPENSATED
    }
}

