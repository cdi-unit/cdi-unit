package io.github.cdiunit.deltaspike;

import jakarta.inject.Inject;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.CdiRunner;
import io.github.cdiunit.InRequestScope;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
public class TestWindowScoped {

    @Inject
    WindowScopedBeanX someWindowScopedBean;

    @Inject
    WindowContext windowContext;

    @Test
    @InRequestScope
    public void testWindowScopedBean() {
        assertThat(someWindowScopedBean).isNotNull();
        assertThat(windowContext).isNotNull();

        {
            windowContext.activateWindow("window1");
            someWindowScopedBean.setValue("Hans");
            assertThat(someWindowScopedBean.getValue()).isEqualTo("Hans");
        }

        // now we switch it away to another 'window'
        {
            windowContext.activateWindow("window2");
            assertThat(someWindowScopedBean.getValue()).isNull();
            someWindowScopedBean.setValue("Karl");
            assertThat(someWindowScopedBean.getValue()).isEqualTo("Karl");
        }

        // and now back to the first window
        {
            windowContext.activateWindow("window1");

            // which must still contain the old value
            assertThat(someWindowScopedBean.getValue()).isEqualTo("Hans");
        }

        // and again back to the second window
        {
            windowContext.activateWindow("window2");

            // which must still contain the old value of the 2nd window
            assertThat(someWindowScopedBean.getValue()).isEqualTo("Karl");
        }
    }

}
