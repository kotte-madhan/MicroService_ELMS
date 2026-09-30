package com.elms.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Utility class for managing correlation IDs across service calls
 * Enables distributed tracing across microservices
 */
@Slf4j
public class CorrelationIdUtil {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /**
     * Get correlation ID from current request or generate a new one
     */
    public static String getCorrelationId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String correlationId = request.getHeader(CORRELATION_ID_HEADER);
                if (correlationId != null && !correlationId.isEmpty()) {
                    return correlationId;
                }
            }
        } catch (Exception e) {
            log.debug("Unable to get correlation ID from request context");
        }

        return generateCorrelationId();
    }

    /**
     * Generate a new correlation ID
     */
    public static String generateCorrelationId() {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

