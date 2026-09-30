package com.elms.common.saga;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Saga Event for tracking saga execution steps
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaEvent implements Serializable {

    @JsonProperty("saga_event_id")
    private String sagaEventId;

    @JsonProperty("saga_id")
    private String sagaId;

    @JsonProperty("step_number")
    private Integer stepNumber;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("action")
    private String action;

    @JsonProperty("status")
    private SagaEventStatus status;

    @JsonProperty("response_data")
    private String responseData;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("correlation_id")
    private String correlationId;

    /**
     * Saga Event Status
     */
    public enum SagaEventStatus {
        PENDING,
        EXECUTING,
        SUCCESS,
        FAILED,
        COMPENSATING,
        COMPENSATED
    }
}

