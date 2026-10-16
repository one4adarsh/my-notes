package org.example.exception;

/** Thrown on an illegal state transition (e.g. cancel after pickup, deliver before pickup). */
public class InvalidStateTransitionException extends QuickCommerceException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
