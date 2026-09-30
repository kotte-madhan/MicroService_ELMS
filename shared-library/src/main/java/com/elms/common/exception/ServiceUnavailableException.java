package com.elms.common.exception;

/**
 * Exception for service unavailable scenarios (HTTP 503)
 */
public class ServiceUnavailableException extends ELMSException {

    public ServiceUnavailableException(String message) {
        super(message, 503, "SERVICE_UNAVAILABLE");
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause, 503, "SERVICE_UNAVAILABLE");
    }
}

