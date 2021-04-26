//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package javax2.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public interface AnnotatedField<X> extends AnnotatedMember<X> {

	class Impl<Y> implements AnnotatedField<Y> {
		private final jakarta.enterprise.inject.spi.AnnotatedField<Y> delegate;

		public Impl(jakarta.enterprise.inject.spi.AnnotatedField<Y> delegate) {
			this.delegate = delegate;
		}

		@Override
		public jakarta.enterprise.inject.spi.AnnotatedField<Y> getDelegate() {
			return delegate;
		}
	}

	jakarta.enterprise.inject.spi.AnnotatedField<X> getDelegate();

    default Field getJavaMember() {
    	return getDelegate().getJavaMember();
	}

    default <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        T[] annotationsByType = this.getJavaMember().getAnnotationsByType(annotationType);
        return new LinkedHashSet(Arrays.asList(annotationsByType));
    }

	@Override
	default boolean isStatic() {
		return getDelegate().isStatic();
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
