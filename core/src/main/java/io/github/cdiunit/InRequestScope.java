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
package io.github.cdiunit;

import java.lang.annotation.*;

import jakarta.interceptor.InterceptorBinding;

/**
 * Starts a request around the annotated method.
 *
 * <pre>
 * &#064;Test
 * &#064;InRequestScope
 * // This test will be run within the context of a request
 * void testStart() {
 *     starship.start();
 * }
 * </pre>
 *
 */
@InterceptorBinding
@Inherited
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface InRequestScope {

}
