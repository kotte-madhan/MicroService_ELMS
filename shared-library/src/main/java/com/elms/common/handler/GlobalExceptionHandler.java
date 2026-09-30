package com.elms.common.handler;

import com.elms.common.dto.ErrorResponse;
import com.elms.common.exception.ELMSException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global Exception Handler for all ELMS microservices
 * Provides consistent error response format across all services
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle custom ELMS exceptions
     */
    @ExceptionHandler(ELMSException.class)
    public ResponseEntity<ErrorResponse> handleELMSException(
            ELMSException ex,
            HttpServletRequest request) {

        String correlationId = getCorrelationId(request);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatusCode())
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .correlationId(correlationId)
                .build();

        log.error("ELMS Exception [{}]: {} - {}",
                correlationId, ex.getErrorCode(), ex.getMessage(), ex);

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(errorResponse);
    }

    /**
     * Handle validation errors (HTTP 400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String correlationId = getCorrelationId(request);
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("BAD_REQUEST")
                .message("Validation failed")
                .path(request.getRequestURI())
                .correlationId(correlationId)
                .details(errors)
                .build();

        log.warn("Validation Exception [{}]: {}", correlationId, errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle resource not found (HTTP 404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpServletRequest request) {

        String correlationId = getCorrelationId(request);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(404)
                .error("RESOURCE_NOT_FOUND")
                .message("Endpoint not found: " + ex.getRequestURL())
                .path(request.getRequestURI())
                .correlationId(correlationId)
                .build();

        log.warn("No Handler Found [{}]: {}", correlationId, ex.getRequestURL());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    /**
     * Handle generic exceptions (HTTP 500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        String correlationId = getCorrelationId(request);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(500)
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .correlationId(correlationId)
                .build();

        log.error("Unexpected Exception [{}]: {}", correlationId, ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * Get correlation ID from request headers or generate a new one
     */
    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = "REQ-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        }
        return correlationId;
    }
}

