//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package javax2.enterprise.inject.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

public interface Annotated {
    Type getBaseType();

    Set<Type> getTypeClosure();

    <T extends Annotation> T getAnnotation(Class<T> var1);

    <T extends Annotation> Set<T> getAnnotations(Class<T> var1);

    Set<Annotation> getAnnotations();

    boolean isAnnotationPresent(Class<? extends Annotation> var1);
}
