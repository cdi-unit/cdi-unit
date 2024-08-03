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
package io.github.cdiunit.internal.servlet;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;
import io.github.cdiunit.internal.servlet5.ServletAPI5Mocks;
import io.github.cdiunit.internal.servlet6.ServletAPI6Mocks;

public class ServletDiscoveryExtension implements DiscoveryExtension {

    private final boolean usesServlet = ClassLookup.INSTANCE.isPresent("jakarta.servlet.http.HttpServletRequest");
    private final boolean servletApi5 = ClassLookup.INSTANCE.isPresent("jakarta.servlet.http.HttpSessionContext");

    private final boolean requiresServletObjectsProducers = !ClassLookup.INSTANCE
            .isPresent("org.jboss.weld.bean.AbstractSyntheticBean");

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        if (usesServlet) {
            bdc.discoverExtension(this::discoverCdiExtension);
            if (requiresServletObjectsProducers) {
                // If this is an old version of Weld then add the producers
                bdc.discoverExtension(this::discoverServletObjectsProducers);
            }
        }
    }

    private void discoverCdiExtension(Context context) {
        context.processBean(InRequestInterceptor.class);
        context.processBean(InSessionInterceptor.class);
        context.processBean(InConversationInterceptor.class);
        context.processBean(CdiUnitInitialListenerImpl.class);
        if (servletApi5) {
            context.processBean(ServletAPI5Mocks.class);
        } else {
            context.processBean(ServletAPI6Mocks.class);
        }
    }

    private void discoverServletObjectsProducers(final Context context) {
        context.processBean(ServletObjectsProducer.class);
    }

}
