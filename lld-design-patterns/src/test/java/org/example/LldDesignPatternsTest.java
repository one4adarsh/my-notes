package org.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class LldDesignPatternsTest {
    @Test public void lldDesignPatternsHasAGreeting() {
        LldDesignPatterns classUnderTest = new LldDesignPatterns();
        assertNotNull("lld-design-patterns should have a greeting", classUnderTest.getGreeting());
    }
}
