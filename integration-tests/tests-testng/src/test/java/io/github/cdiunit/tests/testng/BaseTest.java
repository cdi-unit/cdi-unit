package io.github.cdiunit.tests.testng;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.testng.annotations.BeforeClass;

import io.github.cdiunit.NgCdiRunner;

import static org.assertj.core.api.Assertions.assertThat;

abstract class BaseTest {

    @Inject
    private BeanManager beanManager;

    public BeanManager getBeanManager() {
        return beanManager;
    }

    @BeforeClass
    void failIfExtendsRunner() {
        assertThat(NgCdiRunner.class.isInstance(this))
                .withFailMessage(String.format("%s MUST NOT extend %s", this.getClass(), NgCdiRunner.class)).isFalse();
    }

}
