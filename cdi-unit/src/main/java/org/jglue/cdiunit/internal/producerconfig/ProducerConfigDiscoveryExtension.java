package org.jglue.cdiunit.internal.producerconfig;

import org.jglue.cdiunit.internal.DiscoveryExtension;

public class ProducerConfigDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		bdc.discoverExtension(this::discoverCdiExtension);
	}

	private void discoverCdiExtension(Context context) {
		try {
			Class.forName("javax.enterprise.inject.spi.ProducerFactory");
			context.extension(new ProducerConfigExtension(context.getTestConfiguration()), ProducerConfigDiscoveryExtension.class.getName());
		} catch (ClassNotFoundException ignore) {
		}
	}

}
