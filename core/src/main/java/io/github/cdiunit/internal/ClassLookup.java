package io.github.cdiunit.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class lookup singleton.
 */
public enum ClassLookup {

    INSTANCE;

    private final ConcurrentMap<String, AtomicReference<Class<?>>> classes = new ConcurrentHashMap<>();

    /**
     * Lookup the class. This method is thread-safe.
     *
     * @param className class name to search
     * @return class instance if found, null otherwise
     */
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> lookup(final String className) {
        return (Class<T>) classes.computeIfAbsent(className, this::lookupAbsent).get();
    }

    public boolean isPresent(final String className) {
        return lookup(className) != null;
    }

    private AtomicReference<Class<?>> lookupAbsent(final String className) {
        try {
            return new AtomicReference<>(Class.forName(className));
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // do nothing
        }
        return new AtomicReference<>(null);
    }

}
