package com.userservice.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String error, String message) {
        super(error, message, HttpStatus.UNAUTHORIZED);
    }
}
