package org.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class LldFlipkartMinutesTest {
    @Test public void lldFlipkartMinutesHasAGreeting() {
        LldFlipkartMinutes classUnderTest = new LldFlipkartMinutes();
        assertNotNull("lld-flipkart-minutes should have a greeting", classUnderTest.getGreeting());
    }
}