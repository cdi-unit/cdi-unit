package org.jglue.cdiunit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

/**
 * This extension uses Weld to {@code @Inject} test instances only.
 * No interceptor/{@code @Observes} support for test instances.
 */
public class CdiInjectExtension implements
        BeforeEachCallback, BeforeAllCallback,
        AfterEachCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // TODO if PER_CLASS, start weld, inject fields of
        context.getRequiredTestInstance();
        // TODO maybe store something for cleanup in ExtensionContext.Store (instead of After*Callback)
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // TODO if PER_METHOD, start weld, inject fields of
        context.getRequiredTestInstance();
        // TODO maybe store something for cleanup in ExtensionContext.Store (instead of After*Callback)
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
