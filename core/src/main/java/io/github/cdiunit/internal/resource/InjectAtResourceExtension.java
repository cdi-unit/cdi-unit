package io.github.cdiunit.internal.resource;

import java.beans.MethodDescriptor;

import jakarta.annotation.Resource;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

import org.jboss.weld.literal.NamedLiteral;

public class InjectAtResourceExtension implements Extension {

    private static final AnnotationLiteral<Inject> INJECT_INSTANCE = new AnnotationLiteral<>() {
        private static final long serialVersionUID = 1L;
    };

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
        final AnnotatedTypeConfigurator<T> builder = pat.configureAnnotatedType();

        builder.filterFields(this::eligibleField).forEach(field -> {
            final AnnotatedField<? super T> annotatedField = field.getAnnotated();
            Resource resource = annotatedField.getAnnotation(Resource.class);
            boolean producesPresent = annotatedField.isAnnotationPresent(Produces.class);
            if (!producesPresent) {
                field.add(INJECT_INSTANCE);
            }

            field.remove(a -> a.annotationType().equals(Resource.class));

            // For field annotations, the default is the field name.
            var name = resource.name();
            if (name.isEmpty()) {
                name = annotatedField.getJavaMember().getName();
            }
            field.add(new NamedLiteral(name));

            if (producesPresent) {
                // For field annotations, the default is the type of the field.
                var type = resource.type();
                if (type == Object.class) {
                    type = annotatedField.getJavaMember().getType();
                }
                final var types = new Class<?>[] { type };
                field.add(Typed.Literal.of(types));
            }
        });

        builder.filterMethods(this::eligibleMethod).forEach(method -> {
            final AnnotatedMethod<? super T> annotatedMethod = method.getAnnotated();
            final var methodDescriptor = new MethodDescriptor(annotatedMethod.getJavaMember());
            Resource resource = annotatedMethod.getAnnotation(Resource.class);
            boolean producesPresent = annotatedMethod.isAnnotationPresent(Produces.class);
            if (!producesPresent) {
                method.add(INJECT_INSTANCE);
            }

            method.remove(a -> a.annotationType().equals(Resource.class));

            // For method annotations, the default is the JavaBeans property name corresponding to the method.
            var name = resource.name();
            if (name.isEmpty()) {
                name = methodDescriptor.getName();
            }
            method.add(new NamedLiteral(name));

            if (producesPresent) {
                // For method annotations, the default is the type of the JavaBeans property.
                var type = resource.type();
                if (type == Object.class) {
                    type = annotatedMethod.getJavaMember().getReturnType();
                }
                final var types = new Class<?>[] { type };
                method.add(Typed.Literal.of(types));
            }
        });
    }

    private <X> boolean eligibleField(AnnotatedField<? super X> field) {
        return !field.isAnnotationPresent(Inject.class) && field.isAnnotationPresent(Resource.class);
    }

    private <X> boolean eligibleMethod(AnnotatedMethod<? super X> method) {
        return !method.isAnnotationPresent(Inject.class) && method.isAnnotationPresent(Resource.class);
    }

}
