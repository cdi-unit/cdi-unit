package io.github.cdiunit.internal.deltaspike;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.deltaspike.data.impl.RepositoryExtension;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class DeltaspikeProxyDiscoveryExtension implements DiscoveryExtension {

    /**
     * DeltaSpike 2.0.x proxies support
     */
    private static final String DELTASPIKE_20x_ANCHOR_CLASS_NAME = "org.apache.deltaspike.proxy.spi.invocation.DeltaSpikeProxyInvocationHandler";

    private final Class<?> proxyModuleAnchor;

    public DeltaspikeProxyDiscoveryExtension() {
        proxyModuleAnchor = Stream.of(
                ClassLookup.INSTANCE.lookup(DELTASPIKE_20x_ANCHOR_CLASS_NAME))
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
