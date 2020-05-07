package org.jglue.cdiunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * <code>&#064;IgnoredClasses</code> prevent classes from being automatically added to the CDI environment.
 *  
 * <pre>
 * &#064;RunWith(CdiRunner.class)
 * &#064;IgnoredClasses(Starship.class) //Starship is discoverable from the unit test but should not (it could be produced elsewhere).
 * class TestStarship {
 * 
 * 	&#064;Inject
 * 	Starship starship; //Starship has an engine.
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
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface IgnoredClasses {
	
	/**
	 * @return Array of classes to make undiscoverable during testing.
	 */
	public Class<?>[] value() default {};
	
	/**
	 * @return Array of class names to make undiscoverable during testing (late binding allows specifying classes that are package visible).
	 */
	public String[] late() default {};
}
