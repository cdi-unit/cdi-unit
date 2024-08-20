/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.spock.tests

import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import jakarta.inject.Named

import io.github.cdiunit.AdditionalClasses

@AdditionalClasses(Producers.class)
@ProducerConfigClass(Object.class)
@ProducerConfigNum(0)
class ProducerConfigSpec extends BaseSpec {

    @Inject
    @Named("a")
    private String valueNamedA

    @Inject
    @Named("object")
    private Object object

    // Producers kept out of the injected test class to avoid Weld circularity warnings:
    static class Producers {
        @Produces
        @Named("a")
        private String getValueA(ProducerConfigNum config) {
            return "A" + config.value()
        }

        @Produces
        @Named("object")
        private Object getObject(ProducerConfigClass config) throws Exception {
            return config.value().getDeclaredConstructor().newInstance()
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
