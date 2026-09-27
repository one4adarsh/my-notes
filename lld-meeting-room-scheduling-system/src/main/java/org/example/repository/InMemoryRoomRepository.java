package org.example.repository;

import org.example.model.Room;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryRoomRepository implements RoomRepository {
    private final Map<String, Room> rooms = new LinkedHashMap<>();

    @Override
    public void save(Room room) {
        rooms.put(room.roomId(), room);
    }

    @Override
    public Optional<Room> findById(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }
}
