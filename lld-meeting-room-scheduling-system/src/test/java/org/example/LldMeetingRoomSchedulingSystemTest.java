package org.example;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class LldMeetingRoomSchedulingSystemTest {
    @Test
    public void lldMeetingRoomSchedulingSystemHasAGreeting() {
        LldMeetingRoomSchedulingSystem classUnderTest = new LldMeetingRoomSchedulingSystem();
        assertNotNull("lld-meeting-room-scheduling-system should have a greeting", classUnderTest.getGreeting());
    }
}