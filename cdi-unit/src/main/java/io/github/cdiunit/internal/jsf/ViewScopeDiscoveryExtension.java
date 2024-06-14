package io.github.cdiunit.internal.jsf;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class ViewScopeDiscoveryExtension implements DiscoveryExtension {

	private final boolean usesJsf = ClassLookup.INSTANCE.isPresent("javax.faces.view.ViewScoped");

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		if (usesJsf) {
			bdc.discoverExtension(this::discoverCdiExtension);
		}
	}

	private void discoverCdiExtension(Context context) {
		context.extension(new ViewScopeExtension(), ViewScopeDiscoveryExtension.class.getName());
	}

}
