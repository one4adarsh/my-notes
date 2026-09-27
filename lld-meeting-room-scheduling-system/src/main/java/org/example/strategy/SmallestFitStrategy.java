package org.example.strategy;

import org.example.model.MeetingRequest;
import org.example.model.Room;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SmallestFitStrategy implements RoomAllocationStrategy {
    @Override
    public Optional<Room> select(List<Room> candidates, MeetingRequest request) {
        return candidates.stream().min(Comparator.comparingInt(Room::capacity));
    }
}
