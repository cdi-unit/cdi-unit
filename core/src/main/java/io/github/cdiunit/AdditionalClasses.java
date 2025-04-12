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

/**
 * <code>&#064;AdditionalClasses</code> adds classes to the CDI environment that are not discovered automatically.
 *
 * <pre>
 * &#064;RunWith(CdiRunner.class)
 * &#064;AdditionalClasses(WarpDrive.class) //WarpDrive is not discoverable from the unit test so explicitly make it available.
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
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AdditionalClasses {

    /**
     * @return Array of classes to make discoverable during testing.
     */
    Class<?>[] value() default {};

    /**
     * @return Array of class names to make discoverable during testing (late binding allows specifying classes that are package
     *         visible).
     */
    String[] late() default {};
}
