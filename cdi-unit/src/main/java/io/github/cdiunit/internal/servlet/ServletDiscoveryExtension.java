package io.github.cdiunit.internal.servlet;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class ServletDiscoveryExtension implements DiscoveryExtension {

	private final boolean usesServlet = ClassLookup.INSTANCE.isPresent("javax.servlet.http.HttpServletRequest");
	// new types were introduced in Servlet API 3.1
	private final boolean servletApi31 = ClassLookup.INSTANCE.isPresent("javax.servlet.ReadListener");

	private final boolean requiresServletObjectsProducers = !ClassLookup.INSTANCE.isPresent("org.jboss.weld.bean.AbstractSyntheticBean");

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
		if (servletApi31) {
			context.processBean("io.github.cdiunit.internal.servlet31.ServletAPI31Mocks");
		} else {
			context.processBean("io.github.cdiunit.internal.servlet30.ServletAPI30Mocks");
		}
	}

	private void discoverServletObjectsProducers(final Context context) {
		context.processBean(ServletObjectsProducer.class);
	}

}
