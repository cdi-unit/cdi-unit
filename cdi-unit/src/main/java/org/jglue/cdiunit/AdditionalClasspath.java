package org.jglue.cdiunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * 
 * <code>&#064;AdditionalClasspath</code> adds all the classes in a particular classpath entry to the CDI environment that are not discovered automatically.
 *  
 * <pre>
 * &#064;RunWith(CdiRunner.class)
 * &#064;AdditionalClasspath(Starfleet.class) //WarpDrive is in the starfleet jar.
 * class TestStarship {
 * 
 * 	&#064;Inject
 * 	Starship starship; //Starship has some resources that are in request scope.
 * 
 * 	&#064;Test
 * 	void testStart() {
 * 		starship.start(); // Going to warp!
 * 	}
 * }
 * </pre>
 * 
 * @author Bryn Cooke
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AdditionalClasspath {
	/**
	 * Array of classes that belong to classpath entries to make discoverable during testing.
	 */
	public Class<?>[] value();
}
