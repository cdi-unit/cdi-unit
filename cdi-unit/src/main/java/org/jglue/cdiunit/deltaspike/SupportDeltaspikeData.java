package org.jglue.cdiunit.deltaspike;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.data.impl.RepositoryExtension;
import org.apache.deltaspike.data.impl.tx.ThreadLocalEntityManagerHolder;
import org.apache.deltaspike.partialbean.impl.PartialBeanBindingExtension;
import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;

@AdditionalClasspaths({RepositoryExtension.class})
@SupportDeltaspikePartialBean
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportDeltaspikeData {

}
