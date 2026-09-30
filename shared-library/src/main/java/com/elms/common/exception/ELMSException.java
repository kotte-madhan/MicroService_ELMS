package com.elms.common.exception;

/**
 * Base custom exception for ELMS services
 */
public class ELMSException extends RuntimeException {

    private final int statusCode;
    private final String errorCode;

    public ELMSException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public ELMSException(String message, Throwable cause, int statusCode, String errorCode) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

