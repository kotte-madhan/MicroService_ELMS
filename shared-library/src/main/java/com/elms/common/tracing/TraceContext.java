package com.elms.common.tracing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * STORY 09 - Trace Context
 * Contains all tracing information for a request/span
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceContext {

    private String traceId;        // Unique trace ID across all services
    private String spanId;         // Unique span ID within the trace
    private String parentSpanId;   // Parent span (for nested calls)
    private String correlationId;  // Correlation ID for business tracing
    private String userId;         // User making the request
    private String serviceName;    // Service processing the request
    private String endpoint;       // API endpoint
    private String method;         // HTTP method

    private long startTime;        // Request start time (ms)
    private long endTime;          // Request end time (ms)
    private long duration;         // Total duration (ms)
    private LocalDateTime timestamp; // Timestamp

    private int statusCode;        // HTTP response status
    private boolean error;         // Whether request failed
    private String errorMessage;   // Error details

    /**
     * Get full trace path for logging
     */
    public String getTracePath() {
        return String.format("[%s -> %s]", traceId, spanId);
    }

    /**
     * Get summary for logging
     */
    public String getSummary() {
        return String.format("Trace: %s, Span: %s, Service: %s, Endpoint: %s, Duration: %dms, Status: %d",
                traceId, spanId, serviceName, endpoint, duration, statusCode);
    }
}

