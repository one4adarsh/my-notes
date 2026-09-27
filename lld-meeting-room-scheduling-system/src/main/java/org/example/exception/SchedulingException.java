package org.example.exception;

public abstract class SchedulingException extends RuntimeException {
    protected SchedulingException(String message) {
        super(message);
    }
}
