package org.example;

import org.example.exception.SchedulingException;
import org.example.model.Meeting;
import org.example.model.Room;
import org.example.repository.InMemoryMeetingRepository;
import org.example.repository.InMemoryRoomRepository;
import org.example.service.MeetingScheduler;
import org.example.strategy.SmallestFitStrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Demo walking through the worked example in README.md.
 */
public class LldMeetingRoomSchedulingSystem {

    public static void main(String[] args) {
        MeetingScheduler scheduler = new MeetingScheduler(
                new InMemoryRoomRepository(),
                new InMemoryMeetingRepository(),
                new SmallestFitStrategy());

        scheduler.addRoom(new Room("A", "Room A", 4));
        scheduler.addRoom(new Room("B", "Room B", 10));
        scheduler.addRoom(new Room("C", "Room C", 20));

        LocalDate today = LocalDate.now();
        Meeting m1 = schedule(scheduler, at(today, 10, 0), at(today, 11, 0), 8);
        Meeting m2 = schedule(scheduler, at(today, 10, 30), at(today, 11, 30), 6);
        schedule(scheduler, at(today, 10, 0), at(today, 12, 0), 15);
        Meeting m4 = schedule(scheduler, at(today, 11, 0), at(today, 12, 0), 8);

        scheduler.cancelMeeting(m1.meetingId());
        System.out.println("Cancelled " + m1.meetingId());
        System.out.println("Room B schedule: " + scheduler.getSchedule("B").stream().map(Meeting::meetingId).toList());
        System.out.println("Room B free 10:00-11:00? " + scheduler.isAvailable("B", at(today, 10, 0), at(today, 11, 0)));
        System.out.println("Remaining: " + m2.meetingId() + " (C), " + m4.meetingId() + " (B)");
    }

    private static Meeting schedule(MeetingScheduler scheduler, LocalDateTime start, LocalDateTime end, int attendees) {
        try {
            Meeting meeting = scheduler.scheduleMeeting(start, end, attendees, "alice");
            System.out.printf("Scheduled %s in room %s (%s - %s, %d attendees)%n",
                    meeting.meetingId(), meeting.roomId(), start.toLocalTime(), end.toLocalTime(), attendees);
            return meeting;
        } catch (SchedulingException e) {
            System.out.printf("Rejected (%s - %s, %d attendees): %s%n",
                    start.toLocalTime(), end.toLocalTime(), attendees, e.getMessage());
            return null;
        }
    }

    private static LocalDateTime at(LocalDate date, int hour, int minute) {
        return LocalDateTime.of(date, LocalTime.of(hour, minute));
    }
}
