package io.kite.semantics;

import io.kite.base.CheckerTest;
import io.kite.semantics.types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class UnaryTest extends CheckerTest {

    @Test
    void incrementInt() {
        var res = checker.visit(parse("""
                {
                    var x = 1
                    ++x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void decrementInt() {
        var res = checker.visit(parse("""
                {
                    var x = 1
                    --x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void incrementDecimal() {
        var res = checker.visit(parse("""
                {
                    var x = 1.1
                    ++x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void decrementDecimal() {
        var res = checker.visit(parse("""
                {
                    var x = 1.1
                    --x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void unaryMinus() {
        var res = checker.visit(parse("""
                {
                    var x = 1
                    -x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void unaryMinusDecimal() {
        var res = checker.visit(parse("""
                {
                    var x = 1.5
                    -x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void notFalse() {
        var res = checker.visit(parse("""
                {
                    var x = false
                    !x 
                }
                """));
        assertEquals(ValueType.Boolean, res);
    }

    @Test
    void notTrue() {
        var res = checker.visit(parse("""
                {
                    var x = true
                    !x 
                }
                """));
        assertEquals(ValueType.Boolean, res);
    }


}
