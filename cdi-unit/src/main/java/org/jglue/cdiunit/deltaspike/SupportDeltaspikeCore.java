package org.jglue.cdiunit.deltaspike;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.impl.scope.DeltaSpikeContextExtension;
import org.jglue.cdiunit.AdditionalClasspaths;

@AdditionalClasspaths({ BeanManagerProvider.class,
		DeltaSpikeContextExtension.class })
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportDeltaspikeCore {

}
