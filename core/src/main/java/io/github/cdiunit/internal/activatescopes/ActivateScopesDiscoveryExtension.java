package io.github.cdiunit.internal.activatescopes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import io.github.cdiunit.ActivateScopes;
import io.github.cdiunit.internal.DiscoveryExtension;

/**
 * Discover ActivateScopes features:
 * <ul>
 * <li>{@link ActivateScopes}</li>
 * <li>{@link ActivateScopes.All}</li>
 * <li>meta annotations</li>
 * </ul>
 */
public class ActivateScopesDiscoveryExtension implements DiscoveryExtension {

    private final Set<Class<? extends Annotation>> scopes = new LinkedHashSet<>();

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        bdc.discoverClass(this::discoverClass);
        bdc.discoverMethod(this::discoverMethod);
        bdc.afterDiscovery(this::afterDiscovery);
    }

    private void discoverClass(Context context, Class<?> cls) {
        discover(context, cls.getAnnotation(ActivateScopes.class));
        discover(context, cls.getAnnotation(ActivateScopes.All.class));
    }

    private void discoverMethod(Context context, Method method) {
        discover(context, method.getAnnotation(ActivateScopes.class));
        discover(context, method.getAnnotation(ActivateScopes.All.class));
    }

    private void discover(Context context, ActivateScopes activateScopes) {
        if (activateScopes == null) {
            return;
        }

        Arrays.stream(activateScopes.value()).forEach(scopes::add);
    }

    private void discover(Context context, ActivateScopes.All activateScopes) {
        if (activateScopes == null) {
            return;
        }

        Arrays.stream(activateScopes.value()).forEach(scope -> discover(context, scope));
    }

    private void afterDiscovery(Context context) {
        if (!scopes.isEmpty()) {
            context.extension(new ScopesExtension(scopes));
        }
    }

}
