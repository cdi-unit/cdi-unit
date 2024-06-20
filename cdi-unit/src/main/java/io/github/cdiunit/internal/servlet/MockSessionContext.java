/*
 *    Copyright 2014 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.internal.servlet;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Mock implementation of <code>HttpSessionContext</code>.
 */
public class MockSessionContext implements HttpSessionContext
{
    public Enumeration getIds()
    {
        return new Vector().elements();
    }

    public HttpSession getSession(String arg0)
    {
        return null;
    }
}
