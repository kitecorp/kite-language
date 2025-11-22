package io.kite.stdlib.functions.utility;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonParseFunctionTest extends RuntimeTest {

    private final FromJsonFunction function = new FromJsonFunction();

    @Test
    void jsonParseNotImplemented() {
        // JsonParseFunction is stubbed out - should throw "not yet implemented" error
        var exception = assertThrows(RuntimeException.class, () ->
                function.call(interpreter, "{\"key\": \"value\"}")
        );
        assertTrue(exception.getMessage().contains("not yet implemented"));
    }

    @Test
    void jsonParseTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
