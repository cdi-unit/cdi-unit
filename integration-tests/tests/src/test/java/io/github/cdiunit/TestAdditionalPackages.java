package io.github.cdiunit;

import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.packagetest.PackageImpl;
import io.github.cdiunit.packagetest.PackageInterface;

import static org.assertj.core.api.Assertions.assertThat;

@AdditionalPackages(PackageInterface.class)
@RunWith(CdiRunner.class)
public class TestAdditionalPackages {

    @Inject
    private PackageInterface p;

    @Test
    public void testResolvedPackage() {
        assertThat(p instanceof PackageImpl).isTrue();
    }

}
