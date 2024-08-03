package io.github.cdiunit;

import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.external.ExternalInterface;

import static org.assertj.core.api.Assertions.assertThat;

@AdditionalClasspaths(ExternalInterface.class)
@RunWith(CdiRunner.class)
public class TestAdditionalClasspaths {

    @Inject
    private ExternalInterface external;

    @Test
    public void testResolvedExternal() {
        assertThat(external).isNotNull();
    }

}
