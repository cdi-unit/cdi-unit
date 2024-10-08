/*
 * Copyright 2011 the original author or authors.
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;

public class LifecycleAwareRequest extends HttpServletRequestWrapper {

    private final CdiUnitInitialListener listener;

    public LifecycleAwareRequest(CdiUnitInitialListener listener, HttpServletRequest request) {
        super(request);
        this.listener = listener;
    }

    private HttpSession getSessionAndNotify(boolean create) {
        HttpSession previousSession = super.getSession(false);
        HttpSession session = super.getSession(create);
        if (previousSession == null && session != null) {
            listener.sessionCreated(new HttpSessionEvent(session));
        }
        return session;
    }

    @Override
    public HttpSession getSession() {
        return getSessionAndNotify(true);
    }

    @Override
    public HttpSession getSession(boolean create) {
        return getSessionAndNotify(create);
    }

}
