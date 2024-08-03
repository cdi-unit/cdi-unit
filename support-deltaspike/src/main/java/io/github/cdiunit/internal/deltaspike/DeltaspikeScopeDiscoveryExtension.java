package io.github.cdiunit.internal.deltaspike;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class DeltaspikeScopeDiscoveryExtension implements DiscoveryExtension {

    private final Class<? extends Annotation> windowScopedAnnotation = ClassLookup.INSTANCE.lookup(
            "org.apache.deltaspike.core.api.scope.WindowScoped");
    private final Class<? extends Annotation> viewAccessScopedAnnotation = ClassLookup.INSTANCE.lookup(
            "org.apache.deltaspike.core.api.scope.ViewAccessScoped");
    private final Class<? extends Annotation> groupedConversationScopedAnnotation = ClassLookup.INSTANCE.lookup(
            "org.apache.deltaspike.core.api.scope.GroupedConversationScoped");

    private final List<Class<?>> supportClassesToLoad = Stream.of(
            "org.apache.deltaspike.core.impl.scope.conversation.ConversationBeanHolder",
            "org.apache.deltaspike.core.impl.scope.viewaccess.ViewAccessBeanHolder",
            "org.apache.deltaspike.core.impl.scope.window.WindowBeanHolder")
            .map(ClassLookup.INSTANCE::lookup)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());

    private final Class<?> windowContext = ClassLookup.INSTANCE.lookup(
            "org.apache.deltaspike.core.spi.scope.window.WindowContext");
    private final Class<?> viewAccessContextManager = ClassLookup.INSTANCE.lookup(
            "org.apache.deltaspike.core.spi.scope.viewaccess.ViewAccessContextManager");
    private final Class<?> groupedConversationManager = ClassLookup.INSTANCE.lookup(
            "org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager");

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        bdc.discoverClass(this::discoverAnnotated);
        bdc.discoverField(this::discoverAnnotated);
        bdc.discoverField(this::discoverField);
        bdc.discoverMethod(this::discoverAnnotated);
    }

    private void discoverAnnotated(Context context, AnnotatedElement e) {
        var matchingAnnotationPresent = isAnnotationPresent(e, windowScopedAnnotation)
                || isAnnotationPresent(e, viewAccessScopedAnnotation)
                || isAnnotationPresent(e, groupedConversationScopedAnnotation);
        if (matchingAnnotationPresent) {
            context.scanPackages(supportClassesToLoad).forEach(context::processBean);
        }
    }

    private static boolean isAnnotationPresent(AnnotatedElement e, Class<? extends Annotation> a) {
        if (e == null || a == null) {
            return false;
        }

        return e.isAnnotationPresent(a);
    }

    private boolean typeMatches(Class<?> t) {
        return (windowContext != null && windowContext.isAssignableFrom(t))
                || (viewAccessContextManager != null && viewAccessContextManager.isAssignableFrom(t))
                || (groupedConversationManager != null && groupedConversationManager.isAssignableFrom(t));
    }

    private void discoverField(Context context, Field field) {
        if (field.isAnnotationPresent(Inject.class) && typeMatches(field.getType())) {
            context.scanPackages(supportClassesToLoad).forEach(context::processBean);
        }
    }

}
