package org.example.strategy;

import org.example.model.MeetingRequest;
import org.example.model.Room;

import java.util.List;
import java.util.Optional;

/**
 * Decides which of the already-suitable (capacity + availability) rooms wins.
 */
public interface RoomAllocationStrategy {
    Optional<Room> select(List<Room> candidates, MeetingRequest request);
}
