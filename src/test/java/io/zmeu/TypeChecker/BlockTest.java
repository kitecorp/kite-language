package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker Block")
public class BlockTest extends CheckerTest {

    @Test
    void testBlock() {
        var actual = eval("""
                var x = 10
                var y = 10
                x*2+y
                """);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testNestedBlock() {
        var actual = eval("""
                var x = 10
                {
                    var x = "hello"
                }
                x * 2
                """);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testScopeChainLookup() {
        var actual = eval("""
                var x = 10
                {
                    var y = 10
                    x+y
                }
                """);
        assertEquals(ValueType.Number, actual);
    }
}
