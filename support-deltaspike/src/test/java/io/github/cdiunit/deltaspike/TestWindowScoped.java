package io.github.cdiunit.deltaspike;

import jakarta.inject.Inject;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.CdiRunner;
import io.github.cdiunit.InRequestScope;

@RunWith(CdiRunner.class)
public class TestWindowScoped {

    @Inject
    WindowScopedBeanX someWindowScopedBean;

    @Inject
    WindowContext windowContext;

    @Test
    @InRequestScope
    public void testWindowScopedBean() {
        Assert.assertNotNull(someWindowScopedBean);
        Assert.assertNotNull(windowContext);

        {
            windowContext.activateWindow("window1");
            someWindowScopedBean.setValue("Hans");
            Assert.assertEquals("Hans", someWindowScopedBean.getValue());
        }

        // now we switch it away to another 'window'
        {
            windowContext.activateWindow("window2");
            Assert.assertNull(someWindowScopedBean.getValue());
            someWindowScopedBean.setValue("Karl");
            Assert.assertEquals("Karl", someWindowScopedBean.getValue());
        }

        // and now back to the first window
        {
            windowContext.activateWindow("window1");

            // which must still contain the old value
            Assert.assertEquals("Hans", someWindowScopedBean.getValue());
        }

        // and again back to the second window
        {
            windowContext.activateWindow("window2");

            // which must still contain the old value of the 2nd window
            Assert.assertEquals("Karl", someWindowScopedBean.getValue());
        }
    }

}
