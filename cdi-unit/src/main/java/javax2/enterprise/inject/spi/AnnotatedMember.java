//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package javax2.enterprise.inject.spi;

import java.lang.reflect.Member;

public interface AnnotatedMember<X> extends Annotated {
    Member getJavaMember();

    boolean isStatic();

    AnnotatedType<X> getDeclaringType();
}
