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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Stereotype;

/**
 * <code>&#064;ProducesAlternative</code> causes a produced variable or
 * method to act as an enabled alternative overriding any other suitable
 * injections.
 *
 * <pre>
 * &#064;Produces
 * &#064;ProducesAlternative
 * // This mock will be used instead!
 * &#064;Mock
 * Engine engine;
 * </pre>
 *
 */
@Stereotype
@Alternative
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ProducesAlternative {

}
