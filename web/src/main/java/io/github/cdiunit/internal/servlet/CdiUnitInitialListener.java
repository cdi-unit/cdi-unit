/*
 * Copyright 2015 the original author or authors.
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
package io.github.cdiunit.internal.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.http.HttpSessionEvent;

/**
 * Enables us to inject initial listener. This interface has all the same methods as
 * WeldListener/WeldInitialListener, but allows us to avoid coding directly against
 * these interfaces (which moved around between Weld 1.x and Weld 3.x).
 *
 */
public interface CdiUnitInitialListener {
    void contextInitialized(ServletContextEvent sce);

    void contextDestroyed(ServletContextEvent sce);

    void sessionCreated(HttpSessionEvent event);

    void sessionDestroyed(HttpSessionEvent event);

    void requestDestroyed(ServletRequestEvent event);

    void requestInitialized(ServletRequestEvent event);
}
