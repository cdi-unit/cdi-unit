package org.jglue.cdiunit.internal.jsf;

import org.jglue.cdiunit.internal.DiscoveryExtension;

public class ViewScopeDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		bdc.discoverExtension(this::discoverCdiExtension);
	}

	private void discoverCdiExtension(Context context) {
		try {
			Class.forName("javax.faces.view.ViewScoped");
			context.extension(new ViewScopeExtension(), ViewScopeDiscoveryExtension.class.getName());
		} catch (ClassNotFoundException ignore) {
		}
	}

}
