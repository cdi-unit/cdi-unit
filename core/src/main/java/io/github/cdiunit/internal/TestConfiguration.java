/*
 * Copyright 2017 the original author or authors.
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

import java.util.Collection;
import java.util.Collections;

import io.github.cdiunit.ActivatedAlternatives;
import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.Isolation;
import io.github.cdiunit.IsolationLevel;

/**
 * Defines how CDI-Unit should initialize Weld.
 */
public class TestConfiguration {

    private final Class<?> testClass;

    private final Collection<Class<?>> additionalClasses;
    private final IsolationLevel isolationLevel;

    public TestConfiguration(Class<?> testClass, Collection<Class<?>> additionalClasses) {
        this.testClass = testClass;
        if (additionalClasses == null) {
            throw new NullPointerException("Expected AdditionalClasses not null.");
        }
        this.additionalClasses = additionalClasses;
        this.isolationLevel = getIsolationLevel(testClass);
    }

    public TestConfiguration(Class<?> testClass) {
        this(testClass, Collections.emptySet());
    }

    /**
     * The class containing the tests
     *
     * @return the class containing the tests.
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * Can be used by special runners to change the initialization of Weld.
     *
     * @return classes to be created by weld if possible (if they are not annotations) or whose {@link AdditionalClasses} or
     *         {@link ActivatedAlternatives} are to be created additionally.
     */
    public Collection<Class<?>> getAdditionalClasses() {
        return additionalClasses;
    }

    /**
     * Returns the isolation level of the tests.
     *
     * @return the isolation level of the tests.
     */
    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    private static IsolationLevel getIsolationLevel(Class<?> testClass) {
        Isolation isolation = testClass.getAnnotation(Isolation.class);
        return isolation == null ? IsolationLevel.PER_METHOD : isolation.value();
    }

}
