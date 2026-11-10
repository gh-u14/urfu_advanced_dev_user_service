package com.userservice.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

    public ConflictException(String error, String message) {
        super(error, message, HttpStatus.CONFLICT);
    }
}
