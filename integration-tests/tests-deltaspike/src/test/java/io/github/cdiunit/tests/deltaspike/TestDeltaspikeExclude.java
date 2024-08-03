package io.github.cdiunit.tests.deltaspike;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.CdiRunner;
import io.github.cdiunit.ContextController;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@AdditionalClasses({ ScopedFactory.class, ExcludeExtension.class })
public class TestDeltaspikeExclude {

    @Inject
    private ContextController contextController;

    @Inject
    private Provider<Scoped> scoped;

    @Mock
    private Runnable disposeListener;

    @Test
    public void testContextController() {
        contextController.openRequest();

        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        assertThat(b2).isEqualTo(b1);
        b1.setDisposedListener(disposeListener);
        contextController.closeRequest();
        Mockito.verify(disposeListener).run();
    }

}
