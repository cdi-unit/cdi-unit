package org.jglue.cdiunit.internal.deltaspike;

import org.apache.deltaspike.data.impl.RepositoryExtension;
import org.jglue.cdiunit.internal.DiscoveryExtension;

import java.util.Collections;

public class DeltaspikeProxyDiscoveryExtension implements DiscoveryExtension {

	private static final String DELTASPIKE_17_18_ANCHOR_CLASS_NAME = "org.apache.deltaspike.proxy.impl.invocation.InterceptorLookup";
	private static final String DELTASPIKE_19_ANCHOR_CLASS_NAME = "org.apache.deltaspike.proxy.spi.invocation.DeltaSpikeProxyInvocationHandler";

	// We don't expect classpath to change between invocations
	private static Class<?> proxyModuleAnchor = null;

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		// support for DeltaSpike 1.7.x and 1.8.x
		discoverDeltaSpikeProxyBeans(DELTASPIKE_17_18_ANCHOR_CLASS_NAME);
		// support for DeltaSpike 1.9.x
		discoverDeltaSpikeProxyBeans(DELTASPIKE_19_ANCHOR_CLASS_NAME);
		if (proxyModuleAnchor != null) {
			// only versions listed above need additional discovery
			bdc.discoverClass(this::discoverClass);
		}
	}

	private void discoverDeltaSpikeProxyBeans(String anchorClassName) {
		if (proxyModuleAnchor != null) {
			return;
		}
		try {
			proxyModuleAnchor = Class.forName(anchorClassName);
		} catch (ClassNotFoundException e) {
			// version not detected
		}
	}

	private void discoverClass(Context context, Class<?> cls) {
		if (cls != RepositoryExtension.class) {
			return;
		}
		// scan proxy module only if RepositoryExtension was requested
		context.scanBeanArchives(Collections.singleton(proxyModuleAnchor))
			.forEach(context::processBean);
	}

}
