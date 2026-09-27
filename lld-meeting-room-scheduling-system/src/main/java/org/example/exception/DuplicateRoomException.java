package org.example.exception;

public class DuplicateRoomException extends SchedulingException {
    public DuplicateRoomException(String roomId) {
        super("room already registered: " + roomId);
    }
}
