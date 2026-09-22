package org.example.lldjira;

import org.junit.Test;
import static org.junit.Assert.*;

public class LldJiraTest {
    @Test public void lldJiraHasAGreeting() {
        LldJira classUnderTest = new LldJira();
        assertNotNull("lld-jira should have a greeting", classUnderTest.getGreeting());
    }
}
