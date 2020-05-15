package org.jglue.cdiunit.internal.testscope;

import org.jglue.cdiunit.internal.DiscoveryExtension;

public class TestScopeDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		bdc.discoverExtension(this::discoverCdiExtension);
	}

	private void discoverCdiExtension(Context context) {
		context.extension(new TestScopeExtension(context.getTestConfiguration()), TestScopeDiscoveryExtension.class.getName());
	}

}
