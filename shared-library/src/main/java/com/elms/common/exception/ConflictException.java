package com.elms.common.exception;

/**
 * Exception for conflict scenarios (HTTP 409)
 */
public class ConflictException extends ELMSException {

    public ConflictException(String message) {
        super(message, 409, "CONFLICT");
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause, 409, "CONFLICT");
    }
}

