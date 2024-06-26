package io.github.cdiunit.tests.testng;

import java.util.stream.Stream;

import org.testng.*;
import org.testng.annotations.Listeners;

import io.github.cdiunit.internal.ClassLookup;

/**
 * Skip test method execution if support for ProducerFactory is not available.
 */
public class RequiresProducerConfig implements IInvokedMethodListener {

    private final boolean supportsProducerFactory = ClassLookup.INSTANCE
            .isPresent("jakarta.enterprise.inject.spi.ProducerFactory");

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        final Class<?> realClass = method.getTestMethod().getTestClass().getRealClass();
        final Listeners listeners = realClass.getAnnotation(Listeners.class);
        if (listeners == null) {
            return;
        }
        final boolean requiresProducerConfig = Stream.of(listeners.value())
                .anyMatch(RequiresProducerConfig.class::isAssignableFrom);
        if (method.isConfigurationMethod() && requiresProducerConfig && !supportsProducerFactory) {
            throw new SkipException("ProducerFactory is not supported");
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    }

}
