package org.jglue.cdiunit.internal;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

/**
 * Defines, how the WeldTestUrlDeployment should initialize Weld.
 *
 * @author aschoerk
 */
public class TestConfiguration {

    /**
     * @param testClass The test class
     * @param testMethod The test method. If null, ProducerConfigExtension can't be used.
     * @param additionalClasses A list of additional classes to add to the deployment
     *        (intended for custom runners)
     */
    @SuppressWarnings("WeakerAccess")
    public TestConfiguration(Class<?> testClass, Method testMethod, Collection<Class<?>> additionalClasses) {
        this.testClass = testClass;
        this.testMethod = testMethod;
        if (additionalClasses == null) {
            throw new NullPointerException("Expected AdditionalClasses not null.");
        }
        this.additionalClasses = additionalClasses;
    }

    /**
     * @param testClass The test class
     * @param testMethod The test method. If null, ProducerConfigExtension can't be used.
     */
    public TestConfiguration(Class<?> testClass, Method testMethod) {
        this(testClass, testMethod, Collections.emptySet());
    }

    private final Class<?> testClass;
    private final Method testMethod;
    private final Collection<Class<?>> additionalClasses;

    /**
     * The class containing the tests
     *
     * @return the class containing the tests.
     */
    public Class<?> getTestClass() {
        return testClass;
    }

    /**
     * The method to start.
     * @return the test-method to start.
     */
    public Method getTestMethod() {
        return testMethod;
    }

    /**
     * Can be used by special runners to change the initialization of Weld.
     *
     * @return classes to be created by weld if possible (if they are not annotations) or whose
     * {@link org.jglue.cdiunit.AdditionalClasses} or {@link org.jglue.cdiunit.ActivatedAlternatives} are to be created additionally.
     */
    public Collection<Class<?>> getAdditionalClasses() {
        return additionalClasses;
    }

}
