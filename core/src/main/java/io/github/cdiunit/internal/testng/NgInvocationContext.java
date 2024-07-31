package io.github.cdiunit.internal.testng;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.testng.IHookCallBack;
import org.testng.ITestResult;

public class NgInvocationContext<H> implements InvocationContext {

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

    public void configure(BeanManager beanManager) {
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
