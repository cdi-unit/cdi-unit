package org.jglue.cdiunit.internal.producerconfig;

import org.jglue.cdiunit.internal.ClassLookup;
import org.jglue.cdiunit.internal.DiscoveryExtension;

public class ProducerConfigDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		if (ClassLookup.INSTANCE.isPresent("javax.enterprise.inject.spi.ProducerFactory")) {
			bdc.discoverExtension(this::discoverCdiExtension);
		}
	}

	private void discoverCdiExtension(Context context) {
		context.extension(new ProducerConfigExtension(context.getTestConfiguration()), ProducerConfigDiscoveryExtension.class.getName());
	}

}
