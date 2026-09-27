package org.example;

import org.example.exception.InvalidTimeSlotException;
import org.example.model.TimeSlot;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeSlotTest {
    private static final LocalDateTime T10 = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime T11 = LocalDateTime.of(2026, 1, 1, 11, 0);
    private static final LocalDateTime T12 = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Test(expected = InvalidTimeSlotException.class)
    public void rejectsStartEqualToEnd() {
        new TimeSlot(T10, T10);
    }

    @Test(expected = InvalidTimeSlotException.class)
    public void rejectsStartAfterEnd() {
        new TimeSlot(T11, T10);
    }

    @Test
    public void detectsPartialOverlap() {
        TimeSlot a = new TimeSlot(T10, T11);
        TimeSlot b = new TimeSlot(T10.plusMinutes(30), T11.plusMinutes(30));
        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a));
    }

    @Test
    public void detectsContainment() {
        TimeSlot outer = new TimeSlot(T10, T12);
        TimeSlot inner = new TimeSlot(T10.plusMinutes(15), T11);
        assertTrue(outer.overlaps(inner));
        assertTrue(inner.overlaps(outer));
    }

    @Test
    public void backToBackSlotsDoNotOverlap() {
        assertFalse(new TimeSlot(T10, T11).overlaps(new TimeSlot(T11, T12)));
        assertFalse(new TimeSlot(T11, T12).overlaps(new TimeSlot(T10, T11)));
    }
}
