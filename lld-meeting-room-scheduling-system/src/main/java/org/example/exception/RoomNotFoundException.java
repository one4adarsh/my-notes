package org.example.exception;

public class RoomNotFoundException extends SchedulingException {
    public RoomNotFoundException(String roomId) {
        super("room not found: " + roomId);
    }
}
