package com.EmployeeService.EmployeeService.Exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.elms.common.dto.ErrorResponse;
import java.time.LocalDateTime;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimistic(OptimisticLockingFailureException ex, WebRequest req) {
        ErrorResponse err = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("UPDATE_CONFLICT")
                .message(ex.getMessage())
                .path(req.getDescription(false))
                .correlationId("REQ-" + UUID.randomUUID())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex, WebRequest req) {
        ErrorResponse err = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("GENERAL_ERROR")
                .message(ex.getMessage())
                .path(req.getDescription(false))
                .correlationId("REQ-" + UUID.randomUUID())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
}

