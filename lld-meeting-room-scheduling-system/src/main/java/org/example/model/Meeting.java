package org.example.model;

import java.util.Objects;

public record Meeting(String meetingId, String roomId, TimeSlot slot, int attendees, String organizerId) {

    public Meeting {
        Objects.requireNonNull(meetingId, "meetingId must not be null");
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(slot, "slot must not be null");
        Objects.requireNonNull(organizerId, "organizerId must not be null");
        if (attendees <= 0) {
            throw new IllegalArgumentException("attendees must be positive, got " + attendees);
        }
    }
}
