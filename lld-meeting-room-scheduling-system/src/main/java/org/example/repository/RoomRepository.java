package org.example.repository;

import org.example.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    void save(Room room);

    Optional<Room> findById(String roomId);

    List<Room> findAll();
}
