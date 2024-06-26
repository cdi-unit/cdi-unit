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
package io.github.cdiunit;

import java.lang.annotation.*;

import jakarta.interceptor.InterceptorBinding;

/**
 * Starts a conversation around the annotated method.
 *
 * <pre>
 * &#064;Test
 * &#064;InConversationScope
 * // This test will be run within the context of a conversation
 * void testStart() {
 *     starship.start();
 * }
 * </pre>
 * <p>
 * Remember to add an implementation of <a href=
 * "http://download.oracle.com/javaee/1.3/api/javax/servlet/http/HttpServletRequest.html"
 * >HttpServletRequest</a> to your test e.g.
 * </p>
 *
 * <pre>
 * &#064;Produces
 * HttpServletRequest getRequest() {
 *     return new DummyHttpRequest();
 * }
 * </pre>
 *
 * @author Bryn Cooke
 */
@InterceptorBinding
@Inherited
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface InConversationScope {

}
