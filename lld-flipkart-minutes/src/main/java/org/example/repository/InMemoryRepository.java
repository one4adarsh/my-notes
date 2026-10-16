package org.example.repository;

import org.example.exception.DuplicateEntityException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Thread-safe in-memory store. Subclasses only supply how to extract the id.
 * <p>
 * {@link #save} is the single atomic guard against duplicate ids: a separate
 * exists()-then-save() in the caller would not be atomic under concurrent onboarding.
 */
public abstract class InMemoryRepository<T> implements Repository<T> {

    private final Map<String, T> store = new ConcurrentHashMap<>();
    private final Function<T, String> idExtractor;

    protected InMemoryRepository(Function<T, String> idExtractor) {
        this.idExtractor = idExtractor;
    }

    @Override
    public void save(T entity) {
        String id = idExtractor.apply(entity);
        if (store.putIfAbsent(id, entity) != null) {
            throw new DuplicateEntityException("Entity with id '" + id + "' already exists");
        }
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Collection<T> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }
}
