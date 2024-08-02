package io.github.cdiunit.mockito;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import io.github.cdiunit.CdiRunner;
import io.github.cdiunit.ProducesAlternative;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
public class TestIgnoreMockedClasses {

    @Inject
    private AService service;

    @Produces
    @ProducesAlternative
    @Mock
    private BService mock;

    @Test
    public void testConfiguration() {
        assertThat(service.hasService()).as("AService must have a mocked BService").isTrue();
    }

}
