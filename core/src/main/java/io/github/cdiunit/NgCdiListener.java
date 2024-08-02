package io.github.cdiunit;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

import io.github.cdiunit.internal.BeanLifecycleHelper;
import io.github.cdiunit.internal.ExceptionUtils;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.WeldHelper;
import io.github.cdiunit.internal.activatescopes.ScopesHelper;
import io.github.cdiunit.internal.testng.NgInvocationContext;

public class NgCdiListener implements IHookable {

    private Weld weld;
    private WeldContainer container;
    private InitialContext initialContext;

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        var method = testResult.getMethod().getConstructorOrMethod().getMethod();
        if (method == null) {
            // invoke default callback when running a constructor
            callBack.runTestMethod(testResult);
            return;
        }
        final var target = testResult.getInstance();
        final TestConfiguration testConfig = new TestConfiguration(target.getClass(), method);
        try {
            initializeCdi(testConfig, target);
            var beanManager = container.getBeanManager();
            var ic = new NgInvocationContext<>(callBack, testResult);
            ic.configure(beanManager);
            ic.proceed();
        } catch (Throwable t) {
            testResult.setThrowable(t);
        } finally {
            try {
                shutdownCdi(testConfig, target);
            } catch (Throwable t) {
                testResult.setThrowable(t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeCdi(final TestConfiguration testConfig, final Object target) throws Throwable {
        weld = WeldHelper.configureWeld(testConfig);

        container = weld.initialize();
        BeanManager beanManager = container.getBeanManager();
        CreationalContext creationalContext = beanManager.createCreationalContext(null);
        AnnotatedType annotatedType = beanManager.createAnnotatedType(testConfig.getTestClass());
        InjectionTarget injectionTarget = beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
        injectionTarget.inject(target, creationalContext);

        System.setProperty("java.naming.factory.initial",
                "io.github.cdiunit.internal.naming.CdiUnitContextFactory");
        try {
            initialContext = new InitialContext();
            initialContext.bind("java:comp/BeanManager", beanManager);
        } catch (NamingException e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
        ScopesHelper.activateContexts(container.getBeanManager(), testConfig.getTestMethod());
        BeanLifecycleHelper.invokePostConstruct(testConfig.getTestClass(), target);
    }

    private void shutdownCdi(final TestConfiguration testConfig, final Object target) throws Throwable {
        BeanLifecycleHelper.invokePreDestroy(testConfig.getTestClass(), target);
        if (container != null) {
            ScopesHelper.deactivateContexts(container.getBeanManager(), testConfig.getTestMethod());
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
