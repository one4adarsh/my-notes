package org.example.repository;

import java.util.Collection;
import java.util.Optional;

/** Minimal storage abstraction keyed by String id. */
public interface Repository<T> {

    void save(T entity);

    Optional<T> findById(String id);

    Collection<T> findAll();
}
