package org.example.exception;

/** Thrown when onboarding an id that is already registered. */
public class DuplicateEntityException extends QuickCommerceException {

    public DuplicateEntityException(String message) {
        super(message);
    }
}
