package com.userservice.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {

    public BadRequestException(String error, String message) {
        super(error, message, HttpStatus.BAD_REQUEST);
    }
}
