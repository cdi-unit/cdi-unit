package io.github.cdiunit.resource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jakarta.annotation.Resource;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.internal.resource.InjectAtResourceExtension;

/**
 * Enable support for {@link Resource} injection.
 */
@AdditionalClasses({ InjectAtResourceExtension.class })
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportResource {
}
