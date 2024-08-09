/*
 * Copyright 2024 the original author or authors.
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
package io.github.cdiunit.internal.events;

import java.io.Serializable;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import io.github.cdiunit.internal.events.EventsForwardingExtension.ObserverBinding;

@Priority(Interceptor.Priority.LIBRARY_AFTER)
@Interceptor
@ForwardedEvents
class ForwardingInterceptor implements Serializable {

    @Inject
    private EventsForwardingExtension extension;

    @Inject
    BeanManager beanManager;

    @AroundInvoke
    public Object forward(InvocationContext ic) throws Exception {
        ObserverBinding binding = null;
        var params = ic.getParameters();
        if (params.length > 0) {
            var eventType = params[0].getClass();
            binding = extension.getBinding(eventType);
        }
        if (binding == null) {
            return ic.proceed();
        } else {
            binding.invoke(beanManager, params);
            return null;
        }
    }

}
