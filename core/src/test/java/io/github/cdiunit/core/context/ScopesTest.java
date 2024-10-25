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
package io.github.cdiunit.core.context;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;

import org.junit.jupiter.api.Test;

import io.github.cdiunit.ActivateScopes;

import static org.assertj.core.api.Assertions.assertThat;

class ScopesTest {

    @Test
    void annotatedClass() {

        @ActivateScopes(RequestScoped.class)
        class Target {

        }

        var scopes = Scopes.ofTarget(Target.class);
        assertThat(scopes.contains(RequestScoped.class))
                .as("RequestScoped expected")
                .isTrue();
        assertThat(scopes.contains(SessionScoped.class))
                .as("SessionScoped not expected")
                .isFalse();
    }

    @Test
    void annotatedMethod() throws NoSuchMethodException {

        class Target {

            @ActivateScopes(RequestScoped.class)
            void target() {
            }

        }

        var scopes = Scopes.ofTarget(Target.class.getDeclaredMethod("target"));
        assertThat(scopes.contains(RequestScoped.class))
                .as("RequestScoped expected")
                .isTrue();
        assertThat(scopes.contains(SessionScoped.class))
                .as("SessionScoped not expected")
                .isFalse();
    }

    @Test
    void scopeType() {
        var scopes = Scopes.ofTarget(SessionScoped.class);
        assertThat(scopes.contains(SessionScoped.class))
                .as("SessionScoped expected")
                .isTrue();
        assertThat(scopes.contains(RequestScoped.class))
                .as("RequestScoped not expected")
                .isFalse();
    }

    @Test
    void collectionOfScopeTypes() {
        var scopes = Scopes.ofTarget(Set.of(SessionScoped.class, RequestScoped.class));
        assertThat(scopes.contains(SessionScoped.class))
                .as("SessionScoped expected")
                .isTrue();
        assertThat(scopes.contains(RequestScoped.class))
                .as("RequestScoped expected")
                .isTrue();
        assertThat(scopes.contains(ApplicationScoped.class))
                .as("ApplicationScoped not expected")
                .isFalse();
    }

}
