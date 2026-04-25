package com.pipeline.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDistrictException extends RuntimeException {
    public InvalidDistrictException(String message) {
        super(message);
    }
}
