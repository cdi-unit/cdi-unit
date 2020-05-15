package org.jglue.cdiunit.internal.servlet;

import org.jglue.cdiunit.internal.DiscoveryExtension;

public class ServletDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		bdc.discoverExtension(this::discoverCdiExtension);
	}

	private void discoverCdiExtension(Context context) {
		try {
			Class.forName("javax.servlet.http.HttpServletRequest");
			context.processBean(InRequestInterceptor.class);
			context.processBean(InSessionInterceptor.class);
			context.processBean(InConversationInterceptor.class);
			context.processBean(CdiUnitInitialListenerProducer.class);
			context.processBean(MockServletContextImpl.class);
			context.processBean(MockHttpSessionImpl.class);
			context.processBean(MockHttpServletRequestImpl.class);
			context.processBean(MockHttpServletResponseImpl.class);

			// If this is an old version of weld then add the producers
			try {
				Class.forName("org.jboss.weld.bean.AbstractSyntheticBean");
			} catch (ClassNotFoundException e) {
				context.processBean(ServletObjectsProducer.class);
			}
		} catch (ClassNotFoundException ignore) {
		}

	}

}
