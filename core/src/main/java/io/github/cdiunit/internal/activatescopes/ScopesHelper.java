package io.github.cdiunit.internal.activatescopes;

import jakarta.enterprise.inject.spi.BeanManager;

import static io.github.cdiunit.internal.ExceptionUtils.illegalInstantiation;

public final class ScopesHelper {

    private ScopesHelper() throws IllegalAccessException {
        illegalInstantiation();
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
