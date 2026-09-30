package com.elms.common.exception;

/**
 * Exception for rate limiting exceeded (HTTP 429)
 */
public class RateLimitExceededException extends ELMSException {

    public RateLimitExceededException(String message) {
        super(message, 429, "TOO_MANY_REQUESTS");
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause, 429, "TOO_MANY_REQUESTS");
    }
}

