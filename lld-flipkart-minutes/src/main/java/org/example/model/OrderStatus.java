package org.example.model;

/**
 * PENDING ──assign──▶ ASSIGNED ──pickup──▶ PICKED_UP ──deliver──▶ DELIVERED
 *    │                   │
 *    └──cancel──▶ CANCELLED ◀──cancel──┘        (no cancel after PICKED_UP)
 */
public enum OrderStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}
