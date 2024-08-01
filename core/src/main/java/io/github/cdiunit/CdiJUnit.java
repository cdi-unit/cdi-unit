package io.github.cdiunit;

import org.junit.rules.MethodRule;

import io.github.cdiunit.internal.junit4.CdiJUnitRule;

/**
 * CDI Unit supports JUnit via:
 * <li>
 * <ul>
 * JUnit Rules - see {@link CdiJUnitRule}
 * </ul>
 * <ul>
 * JUnit runners - see {@link CdiRunner}
 * </ul>
 * <ul>
 * <a href=
 * "http://javadoc.io/doc/org.mockito/mockito-junit-jupiter/latest/org/mockito/junit/jupiter/MockitoExtension.html">JUnit
 * Jupiter extension</a>
 * </ul>
 * </li>
 */
public class CdiJUnit {

    /**
     * Creates rule instance that initiates CDI Unit.
     *
     * @return the rule instance
     */
    public static MethodRule rule() {
        return new CdiJUnitRule();
    }

}
