//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package javax2.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public interface AnnotatedParameter<X> extends Annotated {

	class Impl<Y> implements AnnotatedParameter<Y> {
		private final jakarta.enterprise.inject.spi.AnnotatedParameter<Y> delegate;

		public Impl(jakarta.enterprise.inject.spi.AnnotatedParameter<Y> delegate) {
			this.delegate = delegate;
		}

		@Override
		public jakarta.enterprise.inject.spi.AnnotatedParameter<Y> getDelegate() {
			return delegate;
		}
	}

	jakarta.enterprise.inject.spi.AnnotatedParameter<X> getDelegate();

	default int getPosition() {
		return getDelegate().getPosition();
	};

    default AnnotatedCallable<X> getDeclaringCallable() {
    	return new AnnotatedCallable.Impl(getDelegate().getDeclaringCallable());
	};

    default Parameter getJavaParameter() {
        Member member = this.getDeclaringCallable().getJavaMember();
        if (!(member instanceof Executable)) {
            throw new IllegalStateException("Parameter does not belong to an executable: " + member);
        } else {
            Executable executable = (Executable)member;
            return executable.getParameters()[this.getPosition()];
        }
    }

    default <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        T[] annotationsByType = this.getJavaParameter().getAnnotationsByType(annotationType);
        return new LinkedHashSet<>(Arrays.asList(annotationsByType));
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
