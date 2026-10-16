package org.example.model;

/**
 * AVAILABLE ──assign──▶ BUSY ──(deliver | order cancelled)──▶ AVAILABLE
 */
public enum PartnerStatus {
    AVAILABLE,
    BUSY
}
