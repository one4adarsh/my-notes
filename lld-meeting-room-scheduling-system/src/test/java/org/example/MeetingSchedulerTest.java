package org.example;

import org.example.service.MeetingScheduler;
import org.example.exception.DuplicateRoomException;
import org.example.exception.InvalidTimeSlotException;
import org.example.exception.MeetingNotFoundException;
import org.example.exception.NoSuitableRoomException;
import org.example.exception.RoomNotFoundException;
import org.example.model.Meeting;
import org.example.model.Room;
import org.example.repository.InMemoryMeetingRepository;
import org.example.repository.InMemoryRoomRepository;
import org.example.strategy.SmallestFitStrategy;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MeetingSchedulerTest {
    private static final LocalDateTime T10_00 = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime T10_30 = LocalDateTime.of(2026, 1, 1, 10, 30);
    private static final LocalDateTime T11_00 = LocalDateTime.of(2026, 1, 1, 11, 0);
    private static final LocalDateTime T11_30 = LocalDateTime.of(2026, 1, 1, 11, 30);
    private static final LocalDateTime T12_00 = LocalDateTime.of(2026, 1, 1, 12, 0);
    private static final String ORGANIZER = "alice";

    private MeetingScheduler scheduler;

    @Before
    public void setUp() {
        scheduler = new MeetingScheduler(
                new InMemoryRoomRepository(), new InMemoryMeetingRepository(), new SmallestFitStrategy());
        scheduler.addRoom(new Room("A", "Room A", 4));
        scheduler.addRoom(new Room("B", "Room B", 10));
        scheduler.addRoom(new Room("C", "Room C", 20));
    }

    @Test
    public void picksSmallestRoomThatFits() {
        Meeting meeting = scheduler.scheduleMeeting(T10_00, T11_00, 8, ORGANIZER);
        assertEquals("B", meeting.roomId());
        assertEquals(8, meeting.attendees());
        assertEquals(ORGANIZER, meeting.organizerId());
    }

    @Test
    public void fallsBackToNextRoomWhenSmallestIsBusy() {
        scheduler.scheduleMeeting(T10_00, T11_00, 8, ORGANIZER);
        Meeting second = scheduler.scheduleMeeting(T10_30, T11_30, 6, ORGANIZER);
        assertEquals("C", second.roomId());
    }

    @Test(expected = NoSuitableRoomException.class)
    public void rejectsWhenAllLargeEnoughRoomsAreBusy() {
        scheduler.scheduleMeeting(T10_00, T11_00, 8, ORGANIZER);   // B
        scheduler.scheduleMeeting(T10_30, T11_30, 6, ORGANIZER);   // C
        scheduler.scheduleMeeting(T10_00, T12_00, 15, ORGANIZER);  // only C fits, but busy
    }

    @Test(expected = NoSuitableRoomException.class)
    public void rejectsWhenNoRoomIsLargeEnough() {
        scheduler.scheduleMeeting(T10_00, T11_00, 50, ORGANIZER);
    }

    @Test
    public void backToBackMeetingsAreAllowedInSameRoom() {
        Meeting first = scheduler.scheduleMeeting(T10_00, T11_00, 8, ORGANIZER);
        Meeting second = scheduler.scheduleMeeting(T11_00, T12_00, 8, ORGANIZER);
        assertEquals("B", first.roomId());
        assertEquals("B", second.roomId());
    }

    @Test(expected = InvalidTimeSlotException.class)
    public void rejectsInvalidInterval() {
        scheduler.scheduleMeeting(T11_00, T10_00, 4, ORGANIZER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNonPositiveAttendees() {
        scheduler.scheduleMeeting(T10_00, T11_00, 0, ORGANIZER);
    }

    @Test
    public void cancelFreesTheRoom() {
        Meeting meeting = scheduler.scheduleMeeting(T10_00, T11_00, 8, ORGANIZER);
        assertFalse(scheduler.isAvailable("B", T10_00, T11_00));

        scheduler.cancelMeeting(meeting.meetingId());

        assertTrue(scheduler.isAvailable("B", T10_00, T11_00));
        assertTrue(scheduler.getSchedule("B").isEmpty());
    }

    @Test(expected = MeetingNotFoundException.class)
    public void cancelUnknownMeetingFails() {
        scheduler.cancelMeeting("nope");
    }

    @Test(expected = MeetingNotFoundException.class)
    public void cancelTwiceFails() {
        Meeting meeting = scheduler.scheduleMeeting(T10_00, T11_00, 8, ORGANIZER);
        scheduler.cancelMeeting(meeting.meetingId());
        scheduler.cancelMeeting(meeting.meetingId());
    }

    @Test
    public void scheduleIsOrderedByStartTime() {
        Meeting later = scheduler.scheduleMeeting(T11_00, T12_00, 8, ORGANIZER);
        Meeting earlier = scheduler.scheduleMeeting(T10_00, T11_00, 8, ORGANIZER);
        assertEquals(List.of(earlier, later), scheduler.getSchedule("B"));
    }

    @Test(expected = RoomNotFoundException.class)
    public void scheduleOfUnknownRoomFails() {
        scheduler.getSchedule("Z");
    }

    @Test(expected = RoomNotFoundException.class)
    public void availabilityOfUnknownRoomFails() {
        scheduler.isAvailable("Z", T10_00, T11_00);
    }

    @Test(expected = DuplicateRoomException.class)
    public void addDuplicateRoomFails() {
        scheduler.addRoom(new Room("A", "Another A", 6));
    }
}
