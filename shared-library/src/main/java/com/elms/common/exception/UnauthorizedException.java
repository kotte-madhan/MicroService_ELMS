package com.elms.common.exception;

/**
 * Exception for authentication failures (HTTP 401)
 */
public class UnauthorizedException extends ELMSException {

    public UnauthorizedException(String message) {
        super(message, 401, "UNAUTHORIZED");
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause, 401, "UNAUTHORIZED");
    }
}

