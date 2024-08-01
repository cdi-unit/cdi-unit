package io.github.cdiunit.internal.junit4;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.ExceptionUtils;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.WeldHelper;
import io.github.cdiunit.internal.activatescopes.ScopesHelper;

public class CdiJUnitRule implements MethodRule {

    private Weld weld;
    private WeldContainer container;
    private InitialContext initialContext;

    private Object testInstance;
    private TestConfiguration testConfiguration;

    private final AtomicBoolean contextsActivated = new AtomicBoolean();

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        this.testInstance = target;
        testConfiguration = new TestConfiguration(testInstance.getClass(), method.getMethod());
        var statement = new ActivateScopes(base, testConfiguration, contextsActivated,
                () -> container.getBeanManager());
        final var defaultStatement = statement;
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                final var testMethod = method.getMethod();
                try {
                    initializeCdi(target);
                    var beanManager = container.getBeanManager();
                    var ic = new JUnitInvocationContext<>(defaultStatement, target, testMethod);
                    ic.configure(beanManager);
                    ic.proceed();
                } finally {
                    shutdownCdi();
                }
            }

        };
    }

    @SuppressWarnings("unchecked")
    private void initializeCdi(Object instance) {
        weld = WeldHelper.configureWeld(testConfiguration);

        container = weld.initialize();
        BeanManager beanManager = container.getBeanManager();
        CreationalContext creationalContext = beanManager.createCreationalContext(null);
        AnnotatedType annotatedType = beanManager.createAnnotatedType(testConfiguration.getTestClass());
        InjectionTarget injectionTarget = beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
        injectionTarget.inject(instance, creationalContext);

        System.setProperty("java.naming.factory.initial",
                "io.github.cdiunit.internal.naming.CdiUnitContextFactory");
        try {
            initialContext = new InitialContext();
            initialContext.bind("java:comp/BeanManager", beanManager);
        } catch (NamingException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        ScopesHelper.activateContexts(container.getBeanManager(), testConfiguration.getTestMethod());
    }

    private void shutdownCdi() {
        if (container != null) {
            ScopesHelper.deactivateContexts(container.getBeanManager(), testConfiguration.getTestMethod());
        }
        if (weld != null) {
            weld.shutdown();
        }
        if (initialContext != null) {
            try {
                initialContext.close();
            } catch (NamingException e) {
                throw ExceptionUtils.asRuntimeException(e);
            }
        }
    }
}
