package io.github.cdiunit.testng;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import io.github.cdiunit.NgCdiRunner;

abstract class BaseTest {

    @Inject
    private BeanManager beanManager;

    public BeanManager getBeanManager() {
        return beanManager;
    }

    @BeforeClass
    void failIfExtendsRunner() {
        Assert.assertFalse(NgCdiRunner.class.isInstance(this),
                String.format("%s MUST NOT extend %s", this.getClass(), NgCdiRunner.class));
    }

}
