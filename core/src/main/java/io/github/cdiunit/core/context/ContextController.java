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

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ContextNotActiveException;

/**
 * ContextController provides programmatic activation and deactivation of contexts.
 *
 * ContextController is dependent scoped for the purposes of activating and deactivating. For example:
 *
 * <pre>
 * &#064;Inject
 * private ContextController&lt;RequestScoped&gt; requestContextController;
 *
 * public void doRequest(String body) {
 *     // activate request context
 *     requestContextController.activate();
 *
 *     // do work in a request context.
 *
 *     // deactivate the request context
 *     requestContextController.deactivate();
 * }
 * </pre>
 *
 * Once the context has been deactivated, you may activate it once again, creating a brand new context. The activated context is
 * bound to the current thread, any injection points targeting a scoped bean will be satisfied with the same scoped objects.
 *
 * @param <T> an annotation representing the scope
 */
public interface ContextController<T extends Annotation> {

    /**
     * Context scope type.
     *
     * @return context scope type.
     */
    Class<? extends T> getScopeType();

    /**
     * Indicates whether there is an active context for a given scope.
     *
     * @return true if there is an active context for a given scope, false otherwise.
     */
    boolean isActive();

    /**
     * Activates a context for the current thread if one is not already active.
     *
     * @return true if the context was activated by this invocation, false if not.
     */
    boolean activate();

    /**
     * Deactivates the current context if it was activated by this context controller. If the context is active but was not
     * activated by this controller, then it may not be deactivated by this controller, meaning this method will do nothing.
     *
     * If the context is not active, a {@linkplain ContextNotActiveException} is thrown.
     *
     * @throws ContextNotActiveException if the context is not active
     */
    void deactivate() throws ContextNotActiveException;

}
