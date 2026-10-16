package org.example.strategy;

import org.example.model.DeliveryPartner;

import java.util.Collection;
import java.util.Optional;

/**
 * Strategy pattern: how to pick one partner out of the currently available ones.
 * Swap implementations (nearest, highest-rated, round-robin) without touching the dispatcher.
 */
public interface PartnerAssignmentStrategy {

    Optional<DeliveryPartner> select(Collection<DeliveryPartner> available);
}
