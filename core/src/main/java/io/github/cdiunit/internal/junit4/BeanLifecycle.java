package io.github.cdiunit.internal.junit4;

import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.BeanLifecycleHelper;
import io.github.cdiunit.internal.TestConfiguration;

public class BeanLifecycle extends Statement {

    private final Statement base;
    private final TestConfiguration testConfiguration;
    private final Object target;

    public BeanLifecycle(Statement base, TestConfiguration testConfiguration, Object target) {
        this.base = base;
        this.testConfiguration = testConfiguration;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            BeanLifecycleHelper.invokePostConstruct(testConfiguration.getTestClass(), target);
            base.evaluate();
        } finally {
            BeanLifecycleHelper.invokePreDestroy(testConfiguration.getTestClass(), target);
        }
    }

}
