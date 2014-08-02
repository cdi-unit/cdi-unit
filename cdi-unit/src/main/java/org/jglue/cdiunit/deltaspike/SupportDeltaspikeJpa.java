package org.jglue.cdiunit.deltaspike;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.jglue.cdiunit.AdditionalClasspaths;

/**
 * Enable support for DeltaSpike jpa in this test.
 * @author bryn
 *
 */
@AdditionalClasspaths({ TransactionContextExtension.class })
@SupportDeltaspikeCore
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportDeltaspikeJpa {

}
