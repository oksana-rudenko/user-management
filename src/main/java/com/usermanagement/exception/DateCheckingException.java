package com.usermanagement.exception;

public class DateCheckingException extends RuntimeException {
    public DateCheckingException(String message) {
        super(message);
    }

    public DateCheckingException(String message, Throwable cause) {
        super(message, cause);
    }
}
