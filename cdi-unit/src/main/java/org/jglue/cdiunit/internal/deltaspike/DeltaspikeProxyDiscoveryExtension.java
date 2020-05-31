package org.jglue.cdiunit.internal.deltaspike;

import org.apache.deltaspike.data.impl.RepositoryExtension;
import org.jglue.cdiunit.internal.ClassLookup;
import org.jglue.cdiunit.internal.DiscoveryExtension;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

public class DeltaspikeProxyDiscoveryExtension implements DiscoveryExtension {

	private static final String DELTASPIKE_17_18_ANCHOR_CLASS_NAME = "org.apache.deltaspike.proxy.impl.invocation.InterceptorLookup";
	private static final String DELTASPIKE_19_ANCHOR_CLASS_NAME = "org.apache.deltaspike.proxy.spi.invocation.DeltaSpikeProxyInvocationHandler";

	/**
	 * The non-null value here means that we have DeltaSpike 1.7.x or 1.8.x in the classpath.
	 */
	private final Class<?> deltaSpike_17x_18x_proxyModuleAnchor =
		ClassLookup.INSTANCE.lookup(DELTASPIKE_17_18_ANCHOR_CLASS_NAME);

	/**
	 * The non-null value here means that we have DeltaSpike 1.9.x in the classpath.
	 */
	private final Class<?> deltaSpike_19x_proxyModuleAnchor =
		ClassLookup.INSTANCE.lookup(DELTASPIKE_19_ANCHOR_CLASS_NAME);

	private final Class<?> proxyModuleAnchor;

	public DeltaspikeProxyDiscoveryExtension() {
		proxyModuleAnchor = Stream.of(
			deltaSpike_19x_proxyModuleAnchor,
			deltaSpike_17x_18x_proxyModuleAnchor
		)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		if (proxyModuleAnchor != null) {
			bdc.discoverClass(this::discoverClass);
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
