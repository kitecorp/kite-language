package io.kite.stdlib.functions.utility;

import io.kite.base.RuntimeTest;
import io.kite.execution.values.NullValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Function 'env' removed due to namespace conflict with common variable names")
class EnvFunctionTest extends RuntimeTest {

    private final EnvFunction function = new EnvFunction();

    @Test
    void envExistingVariable() {
        // PATH should exist on all systems
        var res = function.call(interpreter, "PATH");
        assertNotNull(res);
        assertFalse(res instanceof NullValue);
    }

    @Test
    void envNonExistingVariable() {
        var res = function.call(interpreter, "THIS_VAR_DOES_NOT_EXIST_12345");
        assertTrue(res instanceof NullValue);
    }

    @Test
    void envWithDefault() {
        var res = function.call(interpreter, "THIS_VAR_DOES_NOT_EXIST_12345", "default-value");
        assertEquals("default-value", res);
    }

    @Test
    void envExistingWithDefault() {
        var res = function.call(interpreter, "PATH", "default-value");
        assertNotNull(res);
        assertNotEquals("default-value", res);
    }

    @Test
    void envHome() {
        // HOME (Unix) or USERPROFILE (Windows) should exist
        var resHome = function.call(interpreter, "HOME");
        var resUserProfile = function.call(interpreter, "USERPROFILE");
        // At least one should be non-null
        assertTrue(!(resHome instanceof NullValue) || !(resUserProfile instanceof NullValue));
    }

    @Test
    void envTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void envInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
