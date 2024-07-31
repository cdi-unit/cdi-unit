package io.github.cdiunit.internal.activatescopes;

import jakarta.enterprise.inject.spi.BeanManager;

public final class ScopesHelper {

    private ScopesHelper() throws IllegalAccessException {
        throw new IllegalAccessException("don't instantiate me");
    }

    public static void activateContexts(BeanManager beanManager, Object target) {
        beanManager.getEvent()
                .select(ScopesExtension.ActivateContexts.Literal.INSTANCE)
                .fire(target);
    }

    public static void deactivateContexts(BeanManager beanManager, Object target) {
        beanManager.getEvent()
                .select(ScopesExtension.DeactivateContexts.Literal.INSTANCE)
                .fire(target);
    }

}
