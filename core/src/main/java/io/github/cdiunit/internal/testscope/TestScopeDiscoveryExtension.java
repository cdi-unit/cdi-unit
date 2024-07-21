package io.github.cdiunit.internal.testscope;

import io.github.cdiunit.internal.DiscoveryExtension;

public class TestScopeDiscoveryExtension implements DiscoveryExtension {

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        bdc.discoverExtension(this::discoverCdiExtension);
    }

    private void discoverCdiExtension(Context context) {
        context.extension(new TestScopeExtension(context.getTestConfiguration()));
    }

}
