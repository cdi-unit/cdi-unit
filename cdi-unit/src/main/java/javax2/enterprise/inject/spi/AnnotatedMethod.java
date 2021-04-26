//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package javax2.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface AnnotatedMethod<X> extends AnnotatedCallable<X> {

	class Impl<Y> implements AnnotatedMethod<Y> {
		private final jakarta.enterprise.inject.spi.AnnotatedMethod<Y> delegate;

		public Impl(jakarta.enterprise.inject.spi.AnnotatedMethod<Y> delegate) {
			this.delegate = delegate;
		}

		@Override
		public jakarta.enterprise.inject.spi.AnnotatedMethod<Y> getDelegate() {
			return delegate;
		}
	}

	jakarta.enterprise.inject.spi.AnnotatedMethod<X> getDelegate();

	default Method getJavaMember() {
		return getDelegate().getJavaMember();
	}

    default <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        T[] annotationsByType = this.getJavaMember().getAnnotationsByType(annotationType);
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

	@Override
	default List<javax2.enterprise.inject.spi.AnnotatedParameter<X>> getParameters() {
		return getDelegate().getParameters().stream().map(AnnotatedParameter.Impl::new).collect(Collectors.toList());
	}

	@Override
	default boolean isStatic() {
		return getDelegate().isStatic();
	}

	@Override
	default javax2.enterprise.inject.spi.AnnotatedType<X> getDeclaringType() {
		return new AnnotatedType.Impl(getDelegate().getDeclaringType());
	}
}
