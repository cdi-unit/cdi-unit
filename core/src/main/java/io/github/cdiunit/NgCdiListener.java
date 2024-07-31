package io.github.cdiunit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.*;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import io.github.cdiunit.internal.ExceptionUtils;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.WeldHelper;
import io.github.cdiunit.internal.activatescopes.ScopesHelper;

public class NgCdiListener implements IHookable, IInvokedMethodListener {

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
        try {
            initializeCdi(testResult.getInstance(), method);
            var beanManager = container.getBeanManager();
            var ic = new NgInvocationContext<>(callBack, testResult);
            ic.configure(beanManager);
            ic.proceed();
        } catch (Exception e) {
            testResult.setThrowable(e);
        } finally {
            shutdownCdi(method);
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeCdi(Object instance, final Method method) {
        final TestConfiguration testConfig = new TestConfiguration(instance.getClass(), method);

        weld = WeldHelper.configureWeld(testConfig);

        container = weld.initialize();
        BeanManager beanManager = container.getBeanManager();
        CreationalContext creationalContext = beanManager.createCreationalContext(null);
        AnnotatedType annotatedType = beanManager.createAnnotatedType(testConfig.getTestClass());
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
        ScopesHelper.activateContexts(container.getBeanManager(), method);
    }

    private void shutdownCdi(final Method method) {
        ScopesHelper.deactivateContexts(container.getBeanManager(), method);
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

    static class NgInvocationContext<H> implements InvocationContext {

        private final IHookCallBack callBack;
        private final ITestResult testResult;
        private final Method method;

        private List<Interceptor<?>> interceptors = List.of();
        private Map<String, Object> contextData;
        private BeanManager beanManager;

        private int interceptorIndex;

        public NgInvocationContext(IHookCallBack callBack, ITestResult testResult) {
            this.callBack = callBack;
            this.testResult = testResult;
            this.method = testResult.getMethod().getConstructorOrMethod().getMethod();
        }

        void configure(BeanManager beanManager) {
            if (method == null) {
                return;
            }
            var bindings = Arrays.stream(method.getAnnotations())
                    .filter(o -> beanManager.isInterceptorBinding(o.annotationType()))
                    .toArray(Annotation[]::new);
            if (bindings.length == 0) {
                // skip interceptors since WELD-001311: Interceptor bindings list cannot be empty
                return;
            }
            this.beanManager = beanManager;
            this.interceptors = beanManager.resolveInterceptors(InterceptionType.AROUND_INVOKE, bindings);
        }

        @Override
        public Object getTarget() {
            return testResult.getInstance();
        }

        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }

        @Override
        public Object[] getParameters() {
            return callBack.getParameters();
        }

        @Override
        public void setParameters(Object[] params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> getContextData() {
            if (contextData == null) {
                contextData = new HashMap<>();
            }
            return contextData;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object proceed() throws Exception {
            if (interceptors.size() > interceptorIndex) {
                Interceptor<H> interceptor = null;
                CreationalContext<H> creationalContext = null;
                H interceptorInstance = null;

                try {
                    interceptor = (Interceptor<H>) interceptors.get(interceptorIndex++);
                    creationalContext = beanManager.createCreationalContext(interceptor);
                    interceptorInstance = interceptor.create(creationalContext);

                    return interceptor.intercept(InterceptionType.AROUND_INVOKE, interceptorInstance, this);
                } finally {
                    if (creationalContext != null) {
                        if (interceptorInstance != null && interceptor != null) {
                            interceptor.destroy(interceptorInstance, creationalContext);
                        }

                        creationalContext.release();
                    }
                }
            }

            callBack.runTestMethod(testResult);
            // test methods are void
            return null;
        }

    }

}
