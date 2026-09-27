package org.example.repository;

import org.example.model.Meeting;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository {
    void save(Meeting meeting);

    Optional<Meeting> findById(String meetingId);

    /** Meetings for the room, ordered by start time. */
    List<Meeting> findByRoom(String roomId);

    void delete(String meetingId);
}
