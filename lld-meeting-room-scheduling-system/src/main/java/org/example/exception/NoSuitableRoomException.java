package org.example.exception;

import org.example.model.MeetingRequest;

public class NoSuitableRoomException extends SchedulingException {
    public NoSuitableRoomException(MeetingRequest request) {
        super("no suitable room for " + request.attendees() + " attendees during "
                + request.slot().start() + " - " + request.slot().end());
    }
}
