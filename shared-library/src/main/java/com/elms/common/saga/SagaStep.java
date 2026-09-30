package com.elms.common.saga;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Saga Step Definition
 * Defines individual steps in a saga with their compensation actions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaStep implements Serializable {

    @JsonProperty("step_number")
    private Integer stepNumber;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("action")
    private String action; // e.g., UPDATE_LEAVE_BALANCE, SEND_NOTIFICATION

    @JsonProperty("compensation_action")
    private String compensationAction; // Rollback action for this step

    @JsonProperty("endpoint")
    private String endpoint; // REST endpoint to call

    @JsonProperty("method")
    private String method; // HTTP method (POST, PUT, etc.)

    @JsonProperty("timeout_seconds")
    private Integer timeoutSeconds;

    @JsonProperty("retry_on_failure")
    private Boolean retryOnFailure;

    @JsonProperty("max_retries")
    private Integer maxRetries;

    @JsonProperty("required")
    private Boolean required; // If true, saga fails if this step fails
}

