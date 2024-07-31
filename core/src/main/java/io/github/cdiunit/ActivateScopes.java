package io.github.cdiunit;

import java.lang.annotation.*;

/**
 * Activate listed scopes for the duration of the test.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
@Repeatable(ActivateScopes.All.class)
public @interface ActivateScopes {

    Class<? extends Annotation>[] value();

    /**
     * Container annotation for repeatable {@link ActivateScopes}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Inherited
    @interface All {
        ActivateScopes[] value();
    }

}
