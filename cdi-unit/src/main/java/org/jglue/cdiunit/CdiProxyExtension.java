package org.jglue.cdiunit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

/**
 * This EXPERIMENTAL extension uses Weld to create inner and outer test instances as proxies, with injection
 * support, and limited support for interceptors. Unlike CdiInstanceProxyExtension, inner and outer classes
 * have the same treatment, which allows for a simpler implementation with more predictable behaviour.
 * If test methods are moved from an outer class to an inner class, behaviour should be the same.
 */
public class CdiProxyExtension implements TestInstanceFactory,
        AfterEachCallback, AfterAllCallback {
    @Override
    public Object createTestInstance(
            TestInstanceFactoryContext testInstanceFactoryContext,
            ExtensionContext extensionContext)
            throws TestInstantiationException {
        // TODO start weld

        // TODO get an ordered list of interceptors from the deployment
        // TODO create Javassist proxy with interceptor support for test class (inner or outer)
        // using CDI instances of the interceptors for the test/method's interceptor bindings

        // TODO inject fields of instance

        // TODO maybe store something for cleanup in ExtensionContext.Store (instead of After*Callback)
        return null;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        // TODO stop weld if PER_METHOD
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        // TODO stop weld if PER_CLASS
    }
}
