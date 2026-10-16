package org.example.exception;

/** Base runtime exception for all domain errors in the system. */
public class QuickCommerceException extends RuntimeException {

    public QuickCommerceException(String message) {
        super(message);
    }
}
