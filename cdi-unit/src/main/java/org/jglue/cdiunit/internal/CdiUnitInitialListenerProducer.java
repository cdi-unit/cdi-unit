package org.jglue.cdiunit.internal;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import javax.enterprise.inject.Produces;

import static java.lang.Thread.currentThread;

/**
 * Decides which version of CdiUnitInitialListener to use, based on the Weld version.
 * @author Sean Flanigan
 */
public class CdiUnitInitialListenerProducer
{
    private static final Class<?> listenerClass;

    static {
        // workaround for WELD-1269/WELD-2305 changes
        Class<?> classToUse;
        try {
            classToUse = CdiUnitInitialListenerImpl11.class;
        } catch (NoClassDefFoundError e) {
            try {
                // We use CGLib to create a class which implements CdiUnitInitialListener but extends the Weld 3.0
                // class WeldInitialListener (inheriting all its method implementations) thus avoiding a compile-time
                // dependency on Weld 3.0 (since it's not practical to have Weld 1/2 and Weld 3 on the classpath in a
                // single module).
                Class<?> weldListenerClass = Class.forName("org.jboss.weld.module.web.servlet.WeldInitialListener");
                Enhancer enhancer = new Enhancer();
                enhancer.setClassLoader(currentThread().getContextClassLoader());
                enhancer.setSuperclass(weldListenerClass);
                enhancer.setInterfaces(new Class[] { CdiUnitInitialListener.class } );
                enhancer.setCallbackType(NoOp.class);
                classToUse = enhancer.createClass();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
        listenerClass = classToUse;
    }

    @Produces
    CdiUnitInitialListener produce() throws Exception {
        return (CdiUnitInitialListener) listenerClass.newInstance();
    }
}
