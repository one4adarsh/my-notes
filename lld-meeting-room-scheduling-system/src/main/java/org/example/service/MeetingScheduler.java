package org.example.service;

import org.example.exception.DuplicateRoomException;
import org.example.exception.MeetingNotFoundException;
import org.example.exception.NoSuitableRoomException;
import org.example.exception.RoomNotFoundException;
import org.example.model.Meeting;
import org.example.model.MeetingRequest;
import org.example.model.Room;
import org.example.model.TimeSlot;
import org.example.repository.MeetingRepository;
import org.example.repository.RoomRepository;
import org.example.strategy.RoomAllocationStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Orchestrator: owns the schedule / cancel workflow across rooms and meetings.
 */
public class MeetingScheduler {
    private final RoomRepository rooms;
    private final MeetingRepository meetings;
    private final RoomAllocationStrategy allocationStrategy;

    public MeetingScheduler(RoomRepository rooms, MeetingRepository meetings,
                            RoomAllocationStrategy allocationStrategy) {
        this.rooms = Objects.requireNonNull(rooms);
        this.meetings = Objects.requireNonNull(meetings);
        this.allocationStrategy = Objects.requireNonNull(allocationStrategy);
    }

    public void addRoom(Room room) {
        if (rooms.findById(room.roomId()).isPresent()) {
            throw new DuplicateRoomException(room.roomId());
        }
        rooms.save(room);
    }

    public Meeting scheduleMeeting(LocalDateTime start, LocalDateTime end, int attendees, String organizerId) {
        TimeSlot slot = new TimeSlot(start, end);
        MeetingRequest request = new MeetingRequest(slot, attendees, organizerId);

        List<Room> candidates = rooms.findAll().stream()
                .filter(room -> room.canAccommodate(attendees))
                .filter(room -> isAvailable(room.roomId(), slot))
                .toList();

        Room room = allocationStrategy.select(candidates, request)
                .orElseThrow(() -> new NoSuitableRoomException(request));

        Meeting meeting = new Meeting(UUID.randomUUID().toString(), room.roomId(), slot, attendees, organizerId);
        meetings.save(meeting);
        return meeting;
    }

    public void cancelMeeting(String meetingId) {
        meetings.findById(meetingId).orElseThrow(() -> new MeetingNotFoundException(meetingId));
        meetings.delete(meetingId);
    }

    public boolean isAvailable(String roomId, LocalDateTime start, LocalDateTime end) {
        return isAvailable(roomId, new TimeSlot(start, end));
    }

    public List<Meeting> getSchedule(String roomId) {
        requireRoom(roomId);
        return meetings.findByRoom(roomId);
    }

    private boolean isAvailable(String roomId, TimeSlot slot) {
        requireRoom(roomId);
        return meetings.findByRoom(roomId).stream().noneMatch(m -> m.slot().overlaps(slot));
    }

    private void requireRoom(String roomId) {
        rooms.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
    }
}
