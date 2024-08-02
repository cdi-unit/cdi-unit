package io.github.cdiunit;

import org.junit.rules.MethodRule;

import io.github.cdiunit.internal.junit4.CdiJUnitRule;

/**
 * CDI Unit supports JUnit via:
 * <ul>
 * <li>* JUnit Rules - see {@link CdiJUnitRule} *</li>
 * <li>* JUnit runners - see {@link CdiRunner} *</li>
 * </ul>
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
