/*
 * Copyright 2014 the original author or authors.
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
package io.github.cdiunit;

import java.io.Serializable;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.junit4.CdiRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
public class TestViewScope {
    @Inject
    private Provider<ViewScopedClass> viewScoped;

    @Inject
    private G2ViewScoped g2ViewScoped;

    @Test
    public void testSameBeanEachTime() {
        assertThat(viewScoped.get().getRuntimeId()).isEqualTo(viewScoped.get().getRuntimeId());
    }

    @Test
    public void testTransitiveViewScoped1() {
        // check that bean can be used by more than one test: https://github.com/BrynCooke/cdi-unit/pull/124
        // (ignoring return value)
        g2ViewScoped.getRuntimeId();
    }

    @Test
    public void testTransitiveViewScoped2() {
        // check that bean can be used by more than one test: https://github.com/BrynCooke/cdi-unit/pull/124
        // (ignoring return value)
        g2ViewScoped.getRuntimeId();
    }

    @ViewScoped
    @Named
    static class ViewScopedClass implements Serializable {
        private static int timesConstructed;

        public ViewScopedClass() {
            timesConstructed++;
        }

        int getRuntimeId() {
            return timesConstructed;
        }
    }

    /**
     * Simple view-scoped bean that depends on another view-scoped bean implements a runtime id through the
     * combination of a naive static variable and the runtime id of its dependency..
     */
    @ViewScoped
    @Named
    static class G2ViewScoped implements Serializable {

        @Inject
        private ViewScopedClass g1ViewScoped;
        private static int timesConstructed;

        public G2ViewScoped() {
            timesConstructed++;
        }

        int getRuntimeId() {
            return 1000 * timesConstructed + g1ViewScoped.getRuntimeId();
        }
    }

}
