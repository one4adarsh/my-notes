package org.example.repository;

import org.example.model.Meeting;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryMeetingRepository implements MeetingRepository {
    private final Map<String, Meeting> meetings = new HashMap<>();

    @Override
    public void save(Meeting meeting) {
        meetings.put(meeting.meetingId(), meeting);
    }

    @Override
    public Optional<Meeting> findById(String meetingId) {
        return Optional.ofNullable(meetings.get(meetingId));
    }

    @Override
    public List<Meeting> findByRoom(String roomId) {
        return meetings.values().stream()
                .filter(m -> m.roomId().equals(roomId))
                .sorted(Comparator.comparing(m -> m.slot().start()))
                .toList();
    }

    @Override
    public void delete(String meetingId) {
        meetings.remove(meetingId);
    }
}
