package org.jglue.cdiunit.deltaspike;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.deltaspike.partialbean.impl.PartialBeanBindingExtension;
import org.jglue.cdiunit.AdditionalClasspaths;

@AdditionalClasspaths({PartialBeanBindingExtension.class })
@SupportDeltaspikeCore
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportDeltaspikePartialBean {

}
