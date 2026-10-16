package org.example.repository;

import org.example.model.DeliveryPartner;

import java.util.List;

public class DeliveryPartnerRepository extends InMemoryRepository<DeliveryPartner> {

    public DeliveryPartnerRepository() {
        super(DeliveryPartner::getId);
    }

    /** Snapshot of partners currently AVAILABLE; input to the assignment strategy. */
    public List<DeliveryPartner> findAvailable() {
        return findAll().stream()
                .filter(DeliveryPartner::isAvailable)
                .toList();
    }
}
