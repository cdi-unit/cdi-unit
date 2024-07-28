package io.github.cdiunit.internal;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Vetoed
class ScopesExtension implements Extension {

    private final Set<Class<? extends Annotation>> scopes;
    private List<CdiContext> contexts = List.of();

    ScopesExtension(Set<Class<? extends Annotation>> scopes) {
        this.scopes = scopes;
    }

    void onAfterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        if (scopes == null) {
            return;
        }

        contexts = scopes.stream()
                .map(scope -> new CdiContext(scope, beanManager))
                .collect(Collectors.toList());
        contexts.forEach(event::addContext);
    }

    public void activateContexts() {
        contexts.forEach(CdiContext::activate);
    }

    public void deactivateContexts() {
        contexts.forEach(CdiContext::deactivate);
    }

    void onActivateContexts(@Observes @ActivateContexts Object event) {
        activateContexts();
    }

    void onDeactivateContexts(@Observes @DeactivateContexts Object event) {
        deactivateContexts();
    }

    @Retention(RUNTIME)
    @Target(PARAMETER)
    @Qualifier
    @interface ActivateContexts {

        final class Literal extends AnnotationLiteral<ActivateContexts> implements ActivateContexts {

            private static final long serialVersionUID = 1L;

            public static final Literal INSTANCE = new Literal();

        }

    }

    @Retention(RUNTIME)
    @Target(PARAMETER)
    @Qualifier
    @interface DeactivateContexts {

        final class Literal extends AnnotationLiteral<DeactivateContexts> implements ActivateContexts {

            private static final long serialVersionUID = 1L;

            public static final Literal INSTANCE = new Literal();

        }

    }

}
