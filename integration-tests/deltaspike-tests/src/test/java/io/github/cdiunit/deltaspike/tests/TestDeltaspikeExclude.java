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
package io.github.cdiunit.deltaspike.tests;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.CdiRunner;
import io.github.cdiunit.ContextController;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@AdditionalClasses({ ScopedFactory.class, ExcludeExtension.class })
public class TestDeltaspikeExclude {

    @Inject
    private ContextController contextController;

    @Inject
    private Provider<Scoped> scoped;

    @Mock
    private Runnable disposeListener;

    @Test
    public void testContextController() {
        contextController.openRequest();

        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        assertThat(b2).isEqualTo(b1);
        b1.setDisposedListener(disposeListener);
        contextController.closeRequest();
        Mockito.verify(disposeListener).run();
    }

}
