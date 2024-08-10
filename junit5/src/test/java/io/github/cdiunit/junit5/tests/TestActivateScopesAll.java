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
package io.github.cdiunit.junit5.tests;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.junit.jupiter.api.Test;

import io.github.cdiunit.ActivateScopes;
import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.junit5.tests.beans.CSessionScoped;
import io.github.cdiunit.junit5.tests.beans.DConversationScoped;
import io.github.cdiunit.junit5.tests.beans.Scoped;
import io.github.cdiunit.junit5.tests.beans.ScopedFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@AdditionalClasses(ScopedFactory.class)
@ActivateScopes.All(@ActivateScopes(RequestScoped.class))
class TestActivateScopesAll extends BaseTest {

    @Inject
    private Provider<Scoped> scoped;

    @Inject
    private CSessionScoped sessionScoped;

    @Inject
    private DConversationScoped conversationScoped;

    @Test
    void requestScoped() {
        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        assertThat(b2).isEqualTo(b1);

        b1.setDisposedListener(() -> assertThat(this).isNotNull());
        b2.setDisposedListener(() -> assertThat(this).isNotNull());
    }

    @Test
    void noActiveSessionScope() {
        assertThat(sessionScoped).isNotNull();
        assertThatExceptionOfType(ContextNotActiveException.class).isThrownBy(() -> sessionScoped.getFoo());
    }

    @Test
    @ActivateScopes.All({ @ActivateScopes(SessionScoped.class) })
    void activeSessionScope() {
        assertThat(sessionScoped).isNotNull();
        sessionScoped.setFoo("success");
    }

    @Test
    void noActiveConversationScope() {
        assertThat(conversationScoped).isNotNull();
        assertThatExceptionOfType(ContextNotActiveException.class).isThrownBy(() -> conversationScoped.getFoo());
    }

    @Test
    @ActivateScopes(ConversationScoped.class)
    void activeConversationScope() {
        assertThat(conversationScoped).isNotNull();
        conversationScoped.setFoo("success");
    }

}
