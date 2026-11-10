package com.userservice.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final String error;
    private final HttpStatus status;

    public ApiException(String error, String message, HttpStatus status) {
        super(message);
        this.error = error;
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
