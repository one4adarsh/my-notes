package org.example.exception;

import java.time.LocalDateTime;

public class InvalidTimeSlotException extends SchedulingException {
    public InvalidTimeSlotException(LocalDateTime start, LocalDateTime end) {
        super("start must be before end, got start=" + start + ", end=" + end);
    }
}
