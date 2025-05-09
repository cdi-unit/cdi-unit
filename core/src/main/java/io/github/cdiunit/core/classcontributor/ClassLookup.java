/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.core.classcontributor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class lookup singleton.
 */
public final class ClassLookup {

    private final ConcurrentMap<String, AtomicReference<Class<?>>> classes = new ConcurrentHashMap<>();

    private ClassLookup() {
    }

    /**
     * Class lookup singleton instance.
     *
     * @return singleton instance
     */
    public static ClassLookup getInstance() {
        return SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private static final ClassLookup INSTANCE = new ClassLookup();
    }

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
