package com.elms.common.tracing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * STORY 09 - Distributed Request Tracking
 * Enhanced tracing with Trace ID, Span ID, Correlation ID
 * Enables end-to-end request tracking across all services
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedTracingService {

    public static final String TRACE_ID_HEADER = "X-Trace-ID";
    public static final String SPAN_ID_HEADER = "X-Span-ID";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String USER_ID_HEADER = "X-User-ID";

    public static final String TRACE_ID_MDC = "traceId";
    public static final String SPAN_ID_MDC = "spanId";
    public static final String CORRELATION_ID_MDC = "correlationId";

    /**
     * Create a new trace context for incoming request
     */
    public TraceContext createTraceContext(String userId, String endpoint, String method) {
        String traceId = getOrGenerateTraceId();
        String spanId = generateSpanId();
        String correlationId = getOrGenerateCorrelationId();

        TraceContext context = TraceContext.builder()
                .traceId(traceId)
                .spanId(spanId)
                .correlationId(correlationId)
                .userId(userId)
                .endpoint(endpoint)
                .method(method)
                .serviceName(getServiceName())
                .startTime(System.currentTimeMillis())
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Trace context created: {}", context);
        return context;
    }

    /**
     * Create child span within a trace
     */
    public TraceContext createChildSpan(TraceContext parentContext, String serviceName) {
        return TraceContext.builder()
                .traceId(parentContext.getTraceId())
                .spanId(generateSpanId())
                .parentSpanId(parentContext.getSpanId())
                .correlationId(parentContext.getCorrelationId())
                .userId(parentContext.getUserId())
                .serviceName(serviceName)
                .startTime(System.currentTimeMillis())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Record span completion with timing
     */
    public void recordSpanCompletion(TraceContext context, int statusCode) {
        long duration = System.currentTimeMillis() - context.getStartTime();

        context.setEndTime(System.currentTimeMillis());
        context.setDuration(duration);
        context.setStatusCode(statusCode);

        log.info("Span completed: {} - {} - {}ms - Status: {}",
                context.getSpanId(), context.getServiceName(), duration, statusCode);
    }

    /**
     * Record span error
     */
    public void recordSpanError(TraceContext context, Exception e) {
        context.setError(true);
        context.setErrorMessage(e.getMessage());

        log.error("Span error: {} - {} - {}",
                context.getSpanId(), context.getServiceName(), e.getMessage());
    }

    /**
     * Get or generate Trace ID
     */
    private String getOrGenerateTraceId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String traceId = attributes.getRequest().getHeader(TRACE_ID_HEADER);
                if (traceId != null && !traceId.isEmpty()) {
                    return traceId;
                }
            }
        } catch (Exception e) {
            log.debug("Unable to get trace ID from request context");
        }

        return "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Get or generate Correlation ID
     */
    private String getOrGenerateCorrelationId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String correlationId = attributes.getRequest().getHeader(CORRELATION_ID_HEADER);
                if (correlationId != null && !correlationId.isEmpty()) {
                    return correlationId;
                }
            }
        } catch (Exception e) {
            log.debug("Unable to get correlation ID from request context");
        }

        return "COR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generate new Span ID
     */
    private String generateSpanId() {
        return "SPN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Get service name from environment or default
     */
    private String getServiceName() {
        return System.getProperty("spring.application.name", "unknown");
    }
}

