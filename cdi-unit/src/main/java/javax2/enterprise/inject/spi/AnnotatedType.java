package javax2.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Temporary class until DeltaSpike provides version for Jakarta EE
 * @param <X>
 */
public interface AnnotatedType<X> extends Annotated {

	class Impl<Y> implements AnnotatedType<Y> {
		private final jakarta.enterprise.inject.spi.AnnotatedType<Y> delegate;

		public Impl(jakarta.enterprise.inject.spi.AnnotatedType<Y> delegate) {
			this.delegate = delegate;
		}

		@Override
		public jakarta.enterprise.inject.spi.AnnotatedType<Y> getDelegate() {
			return delegate;
		}
	}

	jakarta.enterprise.inject.spi.AnnotatedType<X> getDelegate();

    default Class<X> getJavaClass() {
    	return getDelegate().getJavaClass();
	};

	default Set<AnnotatedConstructor<X>> getConstructors() {
		return getDelegate().getConstructors().stream().map(AnnotatedConstructor.Impl::new).collect(Collectors.toSet());
	}

	default Set<AnnotatedMethod<? super X>> getMethods() {
		return getDelegate().getMethods().stream().map(AnnotatedMethod.Impl::new).collect(Collectors.toSet());
	}

	default Set<AnnotatedField<? super X>> getFields() {
		return getDelegate().getFields().stream().map(AnnotatedField.Impl::new).collect(Collectors.toSet());
	}

    default <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        T[] annotationsByType = this.getJavaClass().getAnnotationsByType(annotationType);
        return new LinkedHashSet(Arrays.asList(annotationsByType));
    }

	@Override
	default Type getBaseType() {
		return getDelegate().getBaseType();
	}

	@Override
	default Set<Type> getTypeClosure() {
		return getDelegate().getTypeClosure();
	}

	@Override
	default <T extends Annotation> T getAnnotation(Class<T> var1) {
		return getDelegate().getAnnotation(var1);
	}

	@Override
	default Set<Annotation> getAnnotations() {
		return getDelegate().getAnnotations();
	}

	@Override
	default boolean isAnnotationPresent(Class<? extends Annotation> var1) {
		return getDelegate().isAnnotationPresent(var1);
	}
}
