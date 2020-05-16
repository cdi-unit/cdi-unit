package org.jglue.cdiunit.internal;

import org.jglue.cdiunit.Isolation;
import org.jglue.cdiunit.IsolationLevel;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

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
		this.isolationLevel = getIsolationLevel(testClass);
	}

	public TestConfiguration(Class<?> testClass, Method testMethod) {
		this(testClass, testMethod, Collections.emptySet());
	}

	private final Class<?> testClass;
	private Method testMethod;
	private final Collection<Class<?>> additionalClasses;
	private final IsolationLevel isolationLevel;

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
	 *
	 * @return the test-method to start.
	 */
	public Method getTestMethod() {
		return testMethod;
	}

	public void setTestMethod(Method testMethod) {
		this.testMethod = testMethod;
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
