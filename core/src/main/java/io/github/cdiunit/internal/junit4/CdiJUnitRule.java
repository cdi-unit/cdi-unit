package io.github.cdiunit.internal.junit4;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.TestConfiguration;

public class CdiJUnitRule implements MethodRule {

    private final AtomicBoolean contextsActivated = new AtomicBoolean();

    private Object testInstance;
    private TestConfiguration testConfiguration;

    private BeanManager beanManager;

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        this.testInstance = target;
        testConfiguration = new TestConfiguration(testInstance.getClass(), method.getMethod());
        Statement statement = new Statement() {

            @Override
            public void evaluate() throws Throwable {
                var ic = new JUnitInvocationContext<>(base, target, method.getMethod());
                ic.configure(beanManager);
                ic.proceed();
            }

        };
        statement = new BeanLifecycle(statement, testConfiguration, target);
        statement = new ActivateScopes(statement, testConfiguration, contextsActivated, () -> beanManager);
        statement = new NamingContextLifecycle(statement, testConfiguration, () -> beanManager);
        statement = new WeldLifecycle(statement, testConfiguration, target, bm -> this.beanManager = bm);
        return statement;
    }

}
