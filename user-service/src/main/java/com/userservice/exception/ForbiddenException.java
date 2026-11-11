package com.userservice.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String error, String message) {
        super(error, message, HttpStatus.FORBIDDEN);
    }
}
