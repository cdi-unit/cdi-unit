package org.jglue.cdiunit.deltaspike;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.jglue.cdiunit.AdditionalClasspaths;

@AdditionalClasspaths({ TransactionContextExtension.class })
@SupportDeltaspikeCore
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportDeltaspikeJpa {

}
