package org.example;

import org.example.model.OrderStatus;
import org.example.model.PartnerStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LldFlipkartMinutesTest {

    @Test
    public void orderAndPartnerStatusEnumsAreDefined() {
        assertEquals(5, OrderStatus.values().length);
        assertEquals(2, PartnerStatus.values().length);
    }
}