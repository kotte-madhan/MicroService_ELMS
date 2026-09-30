package com.elms.common.exception;

/**
 * Exception for resource not found scenarios (HTTP 404)
 */
public class ResourceNotFoundException extends ELMSException {

    public ResourceNotFoundException(String message) {
        super(message, 404, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, 404, "RESOURCE_NOT_FOUND");
    }
}

