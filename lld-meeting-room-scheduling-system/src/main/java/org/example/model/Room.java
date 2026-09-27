package org.example.model;

import java.util.Objects;

public record Room(String roomId, String name, int capacity) {

    public Room {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive, got " + capacity);
        }
    }

    public boolean canAccommodate(int attendees) {
        return capacity >= attendees;
    }
}
