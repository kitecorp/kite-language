package io.zmeu.TypeChecker;

import io.zmeu.TypeChecker.Types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class UnaryTest extends BaseChecker {

    @Test
    void incrementInt() {
        var res = checker.visit(src("""
                {
                    var x = 1
                    ++x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void decrementInt() {
        var res = checker.visit(src("""
                {
                    var x = 1
                    --x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void incrementDecimal() {
        var res = checker.visit(src("""
                {
                    var x = 1.1
                    ++x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void decrementDecimal() {
        var res = checker.visit(src("""
                {
                    var x = 1.1
                    --x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void unaryMinus() {
        var res = checker.visit(src("""
                {
                    var x = 1
                    -x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void unaryMinusDecimal() {
        var res = checker.visit(src("""
                {
                    var x = 1.5
                    -x
                }
                """));
        assertEquals(ValueType.Number, res);
    }

    @Test
    void notFalse() {
        var res = checker.visit(src("""
                {
                    var x = false
                    !x 
                }
                """));
        assertEquals(ValueType.Boolean, res);
    }

    @Test
    void notTrue() {
        var res = checker.visit(src("""
                {
                    var x = true
                    !x 
                }
                """));
        assertEquals(ValueType.Boolean, res);
    }


}
