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
package io.github.cdiunit.internal.activatescopes;

import jakarta.enterprise.inject.spi.BeanManager;

import static io.github.cdiunit.internal.ExceptionUtils.illegalInstantiation;

public final class ScopesHelper {

    private ScopesHelper() throws IllegalAccessException {
        illegalInstantiation();
    }

    public static void activateContexts(BeanManager beanManager, Object target) {
        beanManager.getEvent()
                .select(ScopesExtension.ActivateContexts.Literal.INSTANCE)
                .fire(target);
    }

    public static void deactivateContexts(BeanManager beanManager, Object target) {
        beanManager.getEvent()
                .select(ScopesExtension.DeactivateContexts.Literal.INSTANCE)
                .fire(target);
    }

}
