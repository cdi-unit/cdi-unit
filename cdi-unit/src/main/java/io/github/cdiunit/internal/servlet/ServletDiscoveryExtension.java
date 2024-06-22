package io.github.cdiunit.internal.servlet;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class ServletDiscoveryExtension implements DiscoveryExtension {

    private final boolean usesServlet = ClassLookup.INSTANCE.isPresent("jakarta.servlet.http.HttpServletRequest");

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
        context.processBean(CdiUnitInitialListenerProducer.class);
        context.processBean(MockServletContextImpl.class);
        context.processBean(MockHttpSessionImpl.class);
        context.processBean(MockHttpServletRequestImpl.class);
        context.processBean(MockHttpServletResponseImpl.class);
    }

    private void discoverServletObjectsProducers(final Context context) {
        context.processBean(ServletObjectsProducer.class);
    }

}
