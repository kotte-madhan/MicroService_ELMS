package com.elms.common.exception;

/**
 * Exception for internal server errors (HTTP 500)
 */
public class InternalServerException extends ELMSException {

    public InternalServerException(String message) {
        super(message, 500, "INTERNAL_SERVER_ERROR");
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause, 500, "INTERNAL_SERVER_ERROR");
    }
}

