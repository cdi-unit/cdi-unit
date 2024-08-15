package io.github.cdiunit.spock.tests

import io.github.cdiunit.AdditionalClasses
import io.github.cdiunit.ProducerConfig
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import jakarta.inject.Named

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@AdditionalClasses(Producers.class)
@ProducerConfigClass(Object.class)
@ProducerConfigNum(0)
class ProducerConfigSpecification extends BaseSpecification {

    @Inject
    @Named("a")
    private String valueNamedA;

    @Inject
    @Named("object")
    private Object object;

    // Producers kept out of the injected test class to avoid Weld circularity warnings:
    static class Producers {
        @Produces
        @Named("a")
        private String getValueA(ProducerConfigNum config) {
            return "A" + config.value();
        }

        @Produces
        @Named("object")
        private Object getObject(ProducerConfigClass config) throws Exception {
            return config.value().getDeclaredConstructor().newInstance();
        }
    }

    def 'testA0'() {
        expect:
        valueNamedA == "A0"
    }

    @ProducerConfigNum(1)
    def 'testA1'() {
        expect:
        valueNamedA == "A1"
    }

    @ProducerConfigNum(2)
    def 'testA2'() {
        expect:
        valueNamedA == "A2"
    }

    def 'testObject'() {
        expect:
        object.getClass() == Object.class
    }

    @ProducerConfigClass(ArrayList.class)
    def 'testArrayList'() {
        expect:
        object.getClass() == ArrayList.class
    }

    @ProducerConfigClass(HashSet.class)
    def 'testHashSet'() {
        expect:
        object.getClass() == HashSet.class
    }

}

// example ProducerConfig annotations
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@ProducerConfig
@interface ProducerConfigNum {
    int value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE])
@ProducerConfig
@interface ProducerConfigClass {
    Class<?> value();
}
