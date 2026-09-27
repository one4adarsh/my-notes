package org.example.model;

import java.util.Objects;

public record MeetingRequest(TimeSlot slot, int attendees, String organizerId) {

    public MeetingRequest {
        Objects.requireNonNull(slot, "slot must not be null");
        Objects.requireNonNull(organizerId, "organizerId must not be null");
        if (attendees <= 0) {
            throw new IllegalArgumentException("attendees must be positive, got " + attendees);
        }
    }
}
