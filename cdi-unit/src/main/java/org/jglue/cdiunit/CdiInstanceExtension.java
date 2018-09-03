package org.jglue.cdiunit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

/**
 * This extension uses Weld to create test instances as CDI beans, with injection,
 * interceptors and {@code @Observes} support.
 * However {@code @Nested} (inner) test classes are not supported.
 */
public class CdiInstanceExtension implements TestInstanceFactory,
        AfterEachCallback, AfterAllCallback {
    @Override
    public Object createTestInstance(
            TestInstanceFactoryContext testInstanceFactoryContext,
            ExtensionContext extensionContext)
            throws TestInstantiationException {
        // TODO if inner class, throw "Nested tests not supported" exception
        // TODO start weld, create instance of test class
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
