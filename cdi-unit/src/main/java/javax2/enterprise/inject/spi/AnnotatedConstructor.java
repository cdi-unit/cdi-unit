//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package javax2.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface AnnotatedConstructor<X> extends AnnotatedCallable<X> {

	class Impl<Y> implements AnnotatedConstructor<Y> {
		private final jakarta.enterprise.inject.spi.AnnotatedConstructor<Y> delegate;

		public Impl(jakarta.enterprise.inject.spi.AnnotatedConstructor<Y> delegate) {
			this.delegate = delegate;
		}

		@Override
		public jakarta.enterprise.inject.spi.AnnotatedConstructor<Y> getDelegate() {
			return delegate;
		}
	}

	jakarta.enterprise.inject.spi.AnnotatedConstructor<X> getDelegate();


	default Constructor<X> getJavaMember() {
		return getDelegate().getJavaMember();
	}

    default <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        T[] annotationsByType = this.getJavaMember().getAnnotationsByType(annotationType);
        return new LinkedHashSet(Arrays.asList(annotationsByType));
    }

	@Override
	default List<javax2.enterprise.inject.spi.AnnotatedParameter<X>> getParameters() {
		return getDelegate().getParameters().stream().map(AnnotatedParameter.Impl::new).collect(Collectors.toList());
	}

	@Override
	default boolean isStatic() {
		return false;
	}

	@Override
	default javax2.enterprise.inject.spi.AnnotatedType<X> getDeclaringType() {
		return new AnnotatedType.Impl<>(getDelegate().getDeclaringType());
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
	default <T extends Annotation> T getAnnotation(Class<T> aClass) {
		return getDelegate().getAnnotation(aClass);
	}

	@Override
	default Set<Annotation> getAnnotations() {
		return getDelegate().getAnnotations();
	}

	@Override
	default boolean isAnnotationPresent(Class<? extends Annotation> aClass) {
		return getDelegate().isAnnotationPresent(aClass);
	}
}
