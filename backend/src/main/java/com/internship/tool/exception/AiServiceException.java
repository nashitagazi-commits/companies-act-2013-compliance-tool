package com.internship.tool.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AiServiceException extends RuntimeException {

    private final boolean isFallback;

    public AiServiceException(String message) {
        super(message);
        this.isFallback = true;
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.isFallback = true;
    }

    public boolean isFallback() {
        return isFallback;
    }
}