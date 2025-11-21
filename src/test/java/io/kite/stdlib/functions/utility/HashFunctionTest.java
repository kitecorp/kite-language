package io.kite.stdlib.functions.utility;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashFunctionTest extends RuntimeTest {

    private final HashFunction function = new HashFunction();

    @Test
    void hashDefaultAlgorithm() {
        var res = (String) function.call(interpreter, "hello");
        // SHA-256 produces 64 hex characters
        assertEquals(64, res.length());
        assertTrue(res.matches("^[a-f0-9]{64}$"));
    }

    @Test
    void hashSHA256() {
        var res = (String) function.call(interpreter, "hello", "SHA-256");
        assertEquals(64, res.length());
        assertTrue(res.matches("^[a-f0-9]{64}$"));
    }

    @Test
    void hashMD5() {
        var res = (String) function.call(interpreter, "hello", "MD5");
        // MD5 produces 32 hex characters
        assertEquals(32, res.length());
        assertTrue(res.matches("^[a-f0-9]{32}$"));
    }

    @Test
    void hashSHA1() {
        var res = (String) function.call(interpreter, "hello", "SHA-1");
        // SHA-1 produces 40 hex characters
        assertEquals(40, res.length());
        assertTrue(res.matches("^[a-f0-9]{40}$"));
    }

    @Test
    void hashConsistent() {
        var res1 = (String) function.call(interpreter, "test");
        var res2 = (String) function.call(interpreter, "test");
        assertEquals(res1, res2);
    }

    @Test
    void hashDifferentInputs() {
        var res1 = (String) function.call(interpreter, "hello");
        var res2 = (String) function.call(interpreter, "world");
        assertNotEquals(res1, res2);
    }

    @Test
    void hashEmptyString() {
        var res = (String) function.call(interpreter, "");
        assertEquals(64, res.length());
    }

    @Test
    void hashTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void hashInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }

    @Test
    void hashInvalidAlgorithm() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "INVALID-ALGO"));
    }
}
