package org.example.strategy;

import org.example.model.DeliveryPartner;

import java.util.Collection;
import java.util.Optional;

/** Default: pick whichever available partner comes first. */
public class FirstAvailablePartnerStrategy implements PartnerAssignmentStrategy {

    @Override
    public Optional<DeliveryPartner> select(Collection<DeliveryPartner> available) {
        return available.stream().findFirst();
    }
}
