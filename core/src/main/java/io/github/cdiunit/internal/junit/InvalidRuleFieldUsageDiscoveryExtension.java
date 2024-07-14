package io.github.cdiunit.internal.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

/**
 * Discovery extension to find and report invalid usage of {@link org.junit.Rule} annotations in CDI context.
 */
public class InvalidRuleFieldUsageDiscoveryExtension implements DiscoveryExtension {

    public static final String INVALID_RULE_USAGE_MESSAGE = "Invalid @Rule usage detected on field %s of class %s. " +
            "To use rules in CDI tests, put the @Rule annotation on a method instead of a field.";

    /**
     * The non-null value here means that we have JUnit in the classpath.
     */
    @SuppressWarnings("unchecked")
    private final Class<? extends Annotation> fieldAnnotation = (Class<? extends Annotation>) ClassLookup.INSTANCE
            .lookup("org.junit.Rule");

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        if (fieldAnnotation == null) {
            return;
        }
        bdc.discoverField(this::discoverField);
    }

    private void discoverField(Context context, Field field) {
        if (field.getAnnotation(fieldAnnotation) == null) {
            return;
        }
        final Class<?> testClass = context.getTestConfiguration().getTestClass();
        final Class<?> declaringClass = field.getDeclaringClass();
        final int fieldModifiers = field.getModifiers();
        final boolean acceptedRuleFieldModifiers = Modifier.isPublic(fieldModifiers) &&
                !Modifier.isStatic(fieldModifiers);
        if (declaringClass.isAssignableFrom(testClass) && acceptedRuleFieldModifiers) {
            final String message = String.format(INVALID_RULE_USAGE_MESSAGE, field.getName(), declaringClass.getName());
            throw new InvalidRuleFieldUsageException(message);
        }
    }

}
