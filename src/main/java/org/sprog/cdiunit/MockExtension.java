package org.sprog.cdiunit;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import org.mockito.MockitoAnnotations;

public class MockExtension implements Extension {
	public <T> void process(@Observes ProcessInjectionTarget<T> event) {
		final InjectionTarget<T> injectionTarget = event.getInjectionTarget();
		event.setInjectionTarget(new InjectionTarget<T>() {

			public T produce(CreationalContext<T> ctx) {
				T o = injectionTarget.produce(ctx);
				MockitoAnnotations.initMocks(o);
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
