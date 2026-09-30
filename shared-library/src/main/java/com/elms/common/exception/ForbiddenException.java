package com.elms.common.exception;

/**
 * Exception for authorization failures (HTTP 403)
 */
public class ForbiddenException extends ELMSException {

    public ForbiddenException(String message) {
        super(message, 403, "FORBIDDEN");
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause, 403, "FORBIDDEN");
    }
}

