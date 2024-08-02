package io.github.cdiunit.junit4;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

abstract class BaseTest {

    @Inject
    private BeanManager beanManager;

    public BeanManager getBeanManager() {
        return beanManager;
    }

}
