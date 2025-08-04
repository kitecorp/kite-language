package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker If")
public class IfTest extends CheckerTest {

    @Test
    void testBlock() {
        var actual = eval("""
                var x = 10
                var y = 10
                if (x<=10) {
                   y = 2
                } else {
                   y = 3
                }
                y
                """);
        assertEquals(ValueType.Number, actual);
    }


}
