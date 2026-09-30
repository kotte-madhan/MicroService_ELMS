package com.elms.common.exception;

/**
 * Exception for validation errors (HTTP 400)
 */
public class BadRequestException extends ELMSException {

    public BadRequestException(String message) {
        super(message, 400, "BAD_REQUEST");
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause, 400, "BAD_REQUEST");
    }
}

