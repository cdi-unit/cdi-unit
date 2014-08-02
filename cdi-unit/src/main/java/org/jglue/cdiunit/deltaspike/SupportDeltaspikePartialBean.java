package org.jglue.cdiunit.deltaspike;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.deltaspike.partialbean.impl.PartialBeanBindingExtension;
import org.jglue.cdiunit.AdditionalClasspaths;

/**
 * Enable support for DeltaSpike jpa partial beans in this test.
 * @author bryn
 *
 */
@AdditionalClasspaths({PartialBeanBindingExtension.class })
@SupportDeltaspikeCore
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportDeltaspikePartialBean {

}
