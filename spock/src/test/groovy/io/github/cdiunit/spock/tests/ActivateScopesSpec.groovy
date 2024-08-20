/*
 * Copyright 2013 the original author or authors.
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

import jakarta.enterprise.context.ContextNotActiveException
import jakarta.enterprise.context.ConversationScoped
import jakarta.enterprise.context.RequestScoped
import jakarta.enterprise.context.SessionScoped
import jakarta.inject.Inject
import jakarta.inject.Provider

import io.github.cdiunit.ActivateScopes
import io.github.cdiunit.AdditionalClasses
import io.github.cdiunit.test.beans.CSessionScoped
import io.github.cdiunit.test.beans.DConversationScoped
import io.github.cdiunit.test.beans.Scoped
import io.github.cdiunit.test.beans.ScopedFactory

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType

@AdditionalClasses(ScopedFactory.class)
@ActivateScopes(RequestScoped.class)
class ActivateScopesSpec extends BaseSpec {

    @Inject
    private Provider<Scoped> scoped

    @Inject
    private CSessionScoped sessionScoped

    @Inject
    private DConversationScoped conversationScoped

    def 'testRequestScoped'() {
        when:
        Scoped b1 = scoped.get()
        Scoped b2 = scoped.get()

        then:
        b2 == b1

        and:
        b1.setDisposedListener({ -> assertThat(this).isNotNull() })
        b2.setDisposedListener({ -> assertThat(this).isNotNull() })
    }

    def 'testNoActiveSessionScope'() {
        expect:
        sessionScoped != null
        assertThatExceptionOfType(ContextNotActiveException.class).isThrownBy({ -> sessionScoped.getFoo() })
    }

    @ActivateScopes.All([
        @ActivateScopes(SessionScoped.class)
    ])
    def 'testActiveSessionScope'() {
        expect:
        sessionScoped != null
        sessionScoped.setFoo("success")
    }

    def 'testNoActiveConversationScope'() {
        expect:
        conversationScoped != null
        assertThatExceptionOfType(ContextNotActiveException.class).isThrownBy({ -> conversationScoped.getFoo() })
    }

    @ActivateScopes(ConversationScoped.class)
    def 'testActiveConversationScope'() {
        expect:
        conversationScoped != null
        conversationScoped.setFoo("success")
    }
}
