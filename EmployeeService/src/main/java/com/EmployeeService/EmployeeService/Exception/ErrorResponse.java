package com.elms.employeeservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private Instant timestamp = Instant.now();
    private int status;
    private String code;
    private String message;
    private String path;
    private String correlationId;
}

