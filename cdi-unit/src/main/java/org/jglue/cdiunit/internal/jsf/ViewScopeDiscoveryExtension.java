package org.jglue.cdiunit.internal.jsf;

import org.jglue.cdiunit.internal.ClassLookup;
import org.jglue.cdiunit.internal.DiscoveryExtension;

public class ViewScopeDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		if (ClassLookup.INSTANCE.isPresent("javax.faces.view.ViewScoped")) {
			bdc.discoverExtension(this::discoverCdiExtension);
		}
	}

	private void discoverCdiExtension(Context context) {
		context.extension(new ViewScopeExtension(), ViewScopeDiscoveryExtension.class.getName());
	}

}
