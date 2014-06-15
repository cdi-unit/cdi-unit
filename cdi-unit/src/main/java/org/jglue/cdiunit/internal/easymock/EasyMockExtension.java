/*
 *    Copyright 2011 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jglue.cdiunit.internal.easymock;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.easymock.EasyMockSupport;

public class EasyMockExtension implements Extension {
	public <T> void process(@Observes ProcessInjectionTarget<T> event) {
		final InjectionTarget<T> injectionTarget = event.getInjectionTarget();
		event.setInjectionTarget(new InjectionTarget<T>() {

			public T produce(CreationalContext<T> ctx) {
				T o = injectionTarget.produce(ctx);
				EasyMockSupport.injectMocks(o);
				return o;
			}

			public void dispose(T instance) {
				injectionTarget.dispose(instance);
			}

			public Set<InjectionPoint> getInjectionPoints() {
				return injectionTarget.getInjectionPoints();
			}

			public void inject(T instance, CreationalContext<T> ctx) {
				injectionTarget.inject(instance, ctx);
			}

			public void postConstruct(T instance) {
				injectionTarget.postConstruct(instance);
			}

			public void preDestroy(T instance) {
				injectionTarget.preDestroy(instance);
			}
		});
	}
}
