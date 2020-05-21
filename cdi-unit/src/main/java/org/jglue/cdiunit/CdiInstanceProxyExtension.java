package org.jglue.cdiunit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

/**
 * This VERY EXPERIMENTAL extension uses Weld to create outer test instances as CDI beans, with injection,
 * interceptors and {@code @Observes} support. Inner test instances are created as proxies, with injection
 * support, and limited support for interceptors.
 * If test methods are moved from an outer class to an inner class, behaviour may change, especially with
 * regards to interceptors.
 */
public class CdiInstanceProxyExtension implements TestInstanceFactory,
        AfterEachCallback, AfterAllCallback {
    @Override
    public Object createTestInstance(
            TestInstanceFactoryContext testInstanceFactoryContext,
            ExtensionContext extensionContext)
            throws TestInstantiationException {
        // TODO if outer class, start weld, create instance of test class and return it

        // TODO get an ordered list of interceptors from the deployment
        // TODO if inner class, create Javassist proxy instance with interceptor support
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
