package org.example.exception;

public class MeetingNotFoundException extends SchedulingException {
    public MeetingNotFoundException(String meetingId) {
        super("meeting not found: " + meetingId);
    }
}
