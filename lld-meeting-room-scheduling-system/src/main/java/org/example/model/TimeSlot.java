package org.example.model;

import org.example.exception.InvalidTimeSlotException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Half-open interval {@code [start, end)}. Owns interval validity and the overlap rule.
 */
public record TimeSlot(LocalDateTime start, LocalDateTime end) {

    public TimeSlot {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (!start.isBefore(end)) {
            throw new InvalidTimeSlotException(start, end);
        }
    }

    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
