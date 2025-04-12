/*
 * Copyright 2013 the original author or authors.
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
 * <code>&#064;AdditionalClasspaths</code> adds all the classes in a particular classpath entry to the CDI environment that are
 * not discovered automatically.
 *
 * <pre>
 * &#064;RunWith(CdiRunner.class)
 * &#064;AdditionalClasspaths(Starfleet.class) //WarpDrive is in the jar that contains the Starfleet class.
 * class TestStarship {
 *
 *     &#064;Inject
 *     Starship starship; //Starship has an engine in the starfleet jar.
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
@Target(ElementType.TYPE)
public @interface AdditionalClasspaths {
    /**
     * @return Array of classes that belong to classpath entries to make discoverable during testing.
     */
    Class<?>[] value();
}
