package org.example.repository;

import org.example.model.Customer;

public class CustomerRepository extends InMemoryRepository<Customer> {

    public CustomerRepository() {
        super(Customer::getId);
    }
}
