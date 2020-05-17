package org.jglue.cdiunit.internal.servlet;

import org.jglue.cdiunit.internal.ClassLookup;
import org.jglue.cdiunit.internal.DiscoveryExtension;

public class ServletDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		if (ClassLookup.INSTANCE.isPresent("javax.servlet.http.HttpServletRequest")) {
			bdc.discoverExtension(this::discoverCdiExtension);
			if (!ClassLookup.INSTANCE.isPresent("org.jboss.weld.bean.AbstractSyntheticBean")) {
				// If this is an old version of weld then add the producers
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
