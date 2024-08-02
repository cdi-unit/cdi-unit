package io.github.cdiunit.tests.testng;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.assertj.core.api.Assertions;
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
        Assertions.assertThat(NgCdiRunner.class.isInstance(this))
                .withFailMessage(String.format("%s MUST NOT extend %s", this.getClass(), NgCdiRunner.class)).isFalse();
    }

}
