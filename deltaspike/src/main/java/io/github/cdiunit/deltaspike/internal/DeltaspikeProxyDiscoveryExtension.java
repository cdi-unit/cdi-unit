/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.deltaspike.internal;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.deltaspike.data.impl.RepositoryExtension;

import io.github.cdiunit.core.classcontributor.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class DeltaspikeProxyDiscoveryExtension implements DiscoveryExtension {

    /**
     * DeltaSpike 2.0.x proxies support
     */
    private static final String DELTASPIKE_20x_ANCHOR_CLASS_NAME = "org.apache.deltaspike.proxy.spi.invocation.DeltaSpikeProxyInvocationHandler";

    private final Class<?> proxyModuleAnchor;

    public DeltaspikeProxyDiscoveryExtension() {
        proxyModuleAnchor = Stream.of(
                ClassLookup.getInstance().lookup(DELTASPIKE_20x_ANCHOR_CLASS_NAME))
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
