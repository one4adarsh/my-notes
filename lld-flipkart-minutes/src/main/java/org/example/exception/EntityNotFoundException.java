package org.example.exception;

/** Thrown when a customer / partner / order id does not exist. */
public class EntityNotFoundException extends QuickCommerceException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
