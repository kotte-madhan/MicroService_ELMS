package com.elms.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base event class for all domain events
 * Provides common fields for event tracking and correlation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DomainEvent implements Serializable {

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("aggregate_id")
    private String aggregateId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("version")
    private Integer version;
}

