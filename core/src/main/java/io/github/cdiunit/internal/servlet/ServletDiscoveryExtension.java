package io.github.cdiunit.internal.servlet;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;
import io.github.cdiunit.internal.servlet5.ServletAPI5Mocks;
import io.github.cdiunit.internal.servlet6.ServletAPI6Mocks;

public class ServletDiscoveryExtension implements DiscoveryExtension {

    private final boolean usesServlet = ClassLookup.INSTANCE.isPresent("jakarta.servlet.http.HttpServletRequest");
    private final boolean servletApi5 = ClassLookup.INSTANCE.isPresent("jakarta.servlet.http.HttpSessionContext");

    private final boolean requiresServletObjectsProducers = !ClassLookup.INSTANCE
            .isPresent("org.jboss.weld.bean.AbstractSyntheticBean");

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        if (usesServlet) {
            bdc.discoverExtension(this::discoverCdiExtension);
            if (requiresServletObjectsProducers) {
                // If this is an old version of Weld then add the producers
                bdc.discoverExtension(this::discoverServletObjectsProducers);
            }
        }
    }

    private void discoverCdiExtension(Context context) {
        context.processBean(InRequestInterceptor.class);
        context.processBean(InSessionInterceptor.class);
        context.processBean(InConversationInterceptor.class);
        context.processBean(CdiUnitInitialListenerImpl.class);
        if (servletApi5) {
            context.processBean(ServletAPI5Mocks.class);
        } else {
            context.processBean(ServletAPI6Mocks.class);
        }
    }

    private void discoverServletObjectsProducers(final Context context) {
        context.processBean(ServletObjectsProducer.class);
    }

}
