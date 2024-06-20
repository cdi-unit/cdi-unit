package io.github.cdiunit.internal.producerconfig;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class ProducerConfigDiscoveryExtension implements DiscoveryExtension {

	private final boolean supportsProducerFactory = ClassLookup.INSTANCE.isPresent("jakarta.enterprise.inject.spi.ProducerFactory");

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
