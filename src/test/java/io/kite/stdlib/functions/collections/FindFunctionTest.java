package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import io.kite.execution.values.NullValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FindFunctionTest extends RuntimeTest {

    private final FindFunction function = new FindFunction();

    @Test
    void findExistingElement() {
        var res = function.call(interpreter, List.of(1, 2, 3, 4, 5), 3);
        assertEquals(3, res);
    }

    @Test
    void findNonExistingElement() {
        var res = function.call(interpreter, List.of(1, 2, 3), 10);
        assertTrue(res instanceof NullValue);
    }

    @Test
    void findString() {
        var res = function.call(interpreter, List.of("apple", "banana", "cherry"), "banana");
        assertEquals("banana", res);
    }

    @Test
    void findInEmptyList() {
        var res = function.call(interpreter, List.of(), 1);
        assertTrue(res instanceof NullValue);
    }

    @Test
    void findFirstOccurrence() {
        var res = function.call(interpreter, List.of(1, 2, 2, 3), 2);
        assertEquals(2, res);
    }

    @Test
    void findNull() {
        // FindFunction skips null values in the list, so searching for null will not find it
        // Use explicit List to avoid varargs wrapping null in List.of() which throws NPE
        var res = function.call(interpreter, Arrays.asList(Arrays.asList(1, null, 3), null));
        assertTrue(res instanceof NullValue);
    }

    @Test
    void findTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2, 3)));
    }

    @Test
    void findInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-a-list", 1));
    }
}
