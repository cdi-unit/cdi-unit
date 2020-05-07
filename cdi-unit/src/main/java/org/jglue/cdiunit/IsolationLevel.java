package org.jglue.cdiunit;

public enum IsolationLevel {
    /**
     * In this isolation level, all test methods of a test class will run in the same Weld instance.
     */
    PER_CLASS,

    /**
     * In this isolation level, each test method will run in a separate Weld instance.
     */
    PER_METHOD
}
