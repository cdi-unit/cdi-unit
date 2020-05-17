package org.jglue.cdiunit.internal.producerconfig;

import org.jglue.cdiunit.internal.ClassLookup;
import org.jglue.cdiunit.internal.DiscoveryExtension;

public class ProducerConfigDiscoveryExtension implements DiscoveryExtension {

	private final boolean supportsProducerFactory = ClassLookup.INSTANCE.isPresent("javax.enterprise.inject.spi.ProducerFactory");

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		if (supportsProducerFactory) {
			bdc.discoverExtension(this::discoverCdiExtension);
		}
	}

	private void discoverCdiExtension(Context context) {
		context.extension(new ProducerConfigExtension(context.getTestConfiguration()), ProducerConfigDiscoveryExtension.class.getName());
	}

}
