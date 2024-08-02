package io.github.cdiunit.internal.junit4;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.runners.model.Statement;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.activatescopes.ScopesHelper;

public class ActivateScopes extends Statement {

    private final Statement next;
    private final TestConfiguration testConfiguration;
    private final AtomicBoolean contextsActivated;
    private final Supplier<BeanManager> beanManager;

    public ActivateScopes(Statement next, TestConfiguration testConfiguration, AtomicBoolean contextsActivated,
            Supplier<BeanManager> beanManager) {
        this.next = next;
        this.testConfiguration = testConfiguration;
        this.contextsActivated = contextsActivated;
        this.beanManager = beanManager;
    }

    @Override
    public void evaluate() throws Throwable {
        final var method = testConfiguration.getTestMethod();
        final var isolationLevel = testConfiguration.getIsolationLevel();
        try {
            if (!contextsActivated.get()) {
                ScopesHelper.activateContexts(beanManager.get(), method);
                contextsActivated.set(true);
            }
            next.evaluate();
        } finally {
            if (contextsActivated.get() && isolationLevel == IsolationLevel.PER_METHOD) {
                contextsActivated.set(false);
                ScopesHelper.deactivateContexts(beanManager.get(), method);
            }
        }
    }

}
