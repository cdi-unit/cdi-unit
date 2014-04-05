/*
 *    Copyright 2011 Bryn Cooke
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
package org.jglue.cdiunit.internal;


/**
 * Convenience class that can be used if trying to use scopes. If more complex
 * mocking is required then it is better to use an existing servlet mock
 * framework.
 * 
 * @author Bryn Cooke
 * 
 */
@CdiUnitServlet
public class MockHttpServletResponseImpl extends com.mockrunner.mock.web.MockHttpServletResponse {

}
