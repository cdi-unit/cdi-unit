//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package javax2.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface AnnotatedCallable<X> extends AnnotatedMember<X> {

	class Impl<Y> implements AnnotatedCallable<Y> {
		private final jakarta.enterprise.inject.spi.AnnotatedCallable<Y> delegate;

		public Impl(jakarta.enterprise.inject.spi.AnnotatedCallable<Y> delegate) {
			this.delegate = delegate;
		}

		@Override
		public jakarta.enterprise.inject.spi.AnnotatedCallable<Y> getDelegate() {
			return delegate;
		}
	}

	jakarta.enterprise.inject.spi.AnnotatedCallable<X> getDelegate();

	default List<javax2.enterprise.inject.spi.AnnotatedParameter<X>> getParameters() {
		return getDelegate().getParameters().stream().map(AnnotatedParameter.Impl::new).collect(Collectors.toList());
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
	default <T extends Annotation> Set<T> getAnnotations(Class<T> var1) {
		return getDelegate().getAnnotations(var1);
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
	default Member getJavaMember() {
		return getDelegate().getJavaMember();
	}

	@Override
	default boolean isStatic() {
		return getDelegate().isStatic();
	}

	@Override
	default javax2.enterprise.inject.spi.AnnotatedType<X> getDeclaringType() {
		return new AnnotatedType.Impl<>(getDelegate().getDeclaringType());
	}
}
