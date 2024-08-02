package io.github.cdiunit.internal.junit4;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.ExceptionUtils;
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
            invokeLifecycleMethods(testConfiguration.getTestClass(), PostConstruct.class, target);
            base.evaluate();
        } finally {
            invokeLifecycleMethods(testConfiguration.getTestClass(), PreDestroy.class, target);
        }
    }

    private void invokeLifecycleMethods(Class<?> targetClass, Class<? extends Annotation> a, Object target) throws Throwable {
        findLifecycleMethods(targetClass, a).forEach(m -> {
            try {
                m.setAccessible(true);
                m.invoke(target);
            } catch (IllegalAccessException e) {
                throw ExceptionUtils.asRuntimeException(e);
            } catch (InvocationTargetException e) {
                var cause = e.getCause();
                if (cause == null) {
                    cause = e;
                }
                throw ExceptionUtils.asRuntimeException(cause);
            }
        });
    }

    private Collection<Method> findLifecycleMethods(Class<?> targetClass, Class<? extends Annotation> a) {
        return Arrays.stream(targetClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(a))
                .filter(m -> m.getParameterCount() == 0)
                .filter(m -> void.class.equals(m.getReturnType()))
                .collect(Collectors.toList());
    }

}
