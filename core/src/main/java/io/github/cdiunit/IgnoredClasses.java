/*
 * Copyright 2018 the original author or authors.
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

/**
 * <code>&#064;IgnoredClasses</code> prevent classes from being automatically added to the CDI environment.
 *
 * <pre>
 * &#064;RunWith(CdiRunner.class)
 * &#064;IgnoredClasses(Starship.class) //Starship is discoverable from the unit test but should not (it could be produced elsewhere).
 * class TestStarship {
 *
 *     &#064;Inject
 *     Starship starship; //Starship has an engine.
 *
 *     &#064;Test
 *     void testStart() {
 *         starship.start(); // Going to warp!
 *     }
 * }
 * </pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
public @interface IgnoredClasses {

    /**
     * @return Array of classes to make undiscoverable during testing.
     */
    public Class<?>[] value() default {};

    /**
     * @return Array of class names to make undiscoverable during testing (late binding allows specifying classes that are
     *         package visible).
     */
    public String[] late() default {};
}
