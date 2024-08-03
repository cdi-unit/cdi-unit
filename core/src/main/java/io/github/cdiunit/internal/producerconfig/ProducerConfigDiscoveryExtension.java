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
package io.github.cdiunit.internal.producerconfig;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class ProducerConfigDiscoveryExtension implements DiscoveryExtension {

    private final boolean supportsProducerFactory = ClassLookup.INSTANCE
            .isPresent("jakarta.enterprise.inject.spi.ProducerFactory");

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        if (supportsProducerFactory) {
            bdc.discoverExtension(this::discoverCdiExtension);
        }
    }

    private void discoverCdiExtension(Context context) {
        context.extension(new ProducerConfigExtension(context.getTestConfiguration()));
    }

}
