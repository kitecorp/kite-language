package cloud.kitelang.stdlib.functions.utility;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonStringifyFunctionTest extends RuntimeTest {

    private final ToJsonFunction function = new ToJsonFunction();

    @Test
    void jsonStringifyNotImplemented() {
        // JsonStringifyFunction is stubbed out - should throw "not yet implemented" error
        var exception = assertThrows(RuntimeException.class, () ->
                function.call(interpreter, Map.of("key", "value"))
        );
        assertTrue(exception.getMessage().contains("not yet implemented"));
    }

    @Test
    void jsonStringifyTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
