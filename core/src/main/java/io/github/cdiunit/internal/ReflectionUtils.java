/*
 * Copyright 2024 the original author or authors.
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
package io.github.cdiunit.internal;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.cdiunit.internal.ExceptionUtils.illegalInstantiation;

public final class ReflectionUtils {

    ReflectionUtils() throws IllegalAccessException {
        illegalInstantiation();
    }

    /**
     * Stream of classes iterating over class hierarchy.
     *
     * Iteration starts at provided class and ascends over superclasses to the root of the class hierarchy.
     *
     * @param aClass class to start iteration from.
     * @return stream of classes iterating over class hierarchy from provided class to the root of the class hierarchy.
     */
    public static Stream<Class<?>> bottomUpClassHierarchy(Class<?> aClass) {
        return StreamSupport.stream(new ClassHierarchySpliterator(aClass), false);
    }

    /**
     * Spliterator walking over class hierarchy.
     *
     * Walk starts at provided class and ascends over superclasses to the root of the class hierarchy.
     */
    static class ClassHierarchySpliterator implements Spliterator<Class<?>> {

        private Class<?> aClass;

        ClassHierarchySpliterator(Class<?> aClass) {
            this.aClass = aClass;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Class<?>> action) {
            if (aClass == null) {
                return false;
            }

            action.accept(aClass);
            aClass = aClass.getSuperclass();
            return true;
        }

        @Override
        public Spliterator<Class<?>> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public int characteristics() {
            return ORDERED | DISTINCT | NONNULL | IMMUTABLE;
        }
    }

}
