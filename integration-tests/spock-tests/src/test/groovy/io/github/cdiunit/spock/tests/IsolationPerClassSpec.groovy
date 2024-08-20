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

import java.util.concurrent.atomic.AtomicInteger

import jakarta.inject.Inject

import io.github.cdiunit.Isolation
import io.github.cdiunit.IsolationLevel
import io.github.cdiunit.test.beans.ApplicationCounter
import spock.lang.Stepwise

@Isolation(IsolationLevel.PER_CLASS)
@Stepwise
class IsolationPerClassSpec extends BaseSpec {

    private static final AtomicInteger counter = new AtomicInteger()

    @Inject
    ApplicationCounter applicationCounter

    def setupSpec() {
        counter.set(0)
    }

    def 'step1'() {
        when:
        int number = applicationCounter.incrementAndGet()

        then:
        number == counter.incrementAndGet()

        when:
        number = applicationCounter.incrementAndGet()

        then:
        number == counter.incrementAndGet()
    }

    def 'step2'() {
        when:
        int number = applicationCounter.incrementAndGet()

        then:
        number == counter.incrementAndGet()

        when:
        number = applicationCounter.incrementAndGet()

        then:
        number == counter.incrementAndGet()
    }

    def 'step3'() {
        when:
        int number = applicationCounter.incrementAndGet()

        then:
        number == counter.incrementAndGet()

        when:
        number = applicationCounter.incrementAndGet()

        then:
        number == counter.incrementAndGet()
    }

    def 'check counters'() {
        when:
        final appNumber = applicationCounter.incrementAndGet()
        final specNumber = counter.incrementAndGet()

        then:
        appNumber == 7
        specNumber == 7
    }
}
