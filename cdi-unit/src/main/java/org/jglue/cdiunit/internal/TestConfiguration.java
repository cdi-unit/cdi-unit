package org.jglue.cdiunit.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines, how the WeldTestUrlDeployment should initialize Weld.
 *
 * @author aschoerk
 */
public class TestConfiguration {

    public TestConfiguration(Class<?> testClass, Method testMethod, Collection<Class<?>> additionalClasses) {
        this.testClass = testClass;
        this.testMethod = testMethod;
        if (additionalClasses == null) {
            throw new NullPointerException("Expected AdditionalClasses not null.");
        }
        this.additionalClasses = additionalClasses;
    }

    public TestConfiguration(Class<?> testClass, Method testMethod) {
        this(testClass, testMethod, new ArrayList<>());
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
     * @AdditionalClasses or @ActivatedAlternatives are to be created additionally.
     */
    public Collection<Class<?>> getAdditionalClasses() {
        return additionalClasses;
    }

}
