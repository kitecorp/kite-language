package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.NullValue;
import cloud.kitelang.syntax.literals.SymbolIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.literals.BooleanLiteral.bool;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.ObjectLiteral.ObjectLiteralPair;
import static cloud.kitelang.syntax.literals.ObjectLiteral.object;

@Slf4j
public class LiteralTest extends RuntimeTest {
    @Test
    void literal() {
        var res = interpreter.visit(10.0);
        Assertions.assertEquals(10.0, res);
    }

    @Test
    void literalDouble() {
        var res = interpreter.visit(10.1);
        Assertions.assertEquals(10.1, res);
    }

    @Test
    void integerLiteral() {
        var res = interpreter.visit(number(10));
        Assertions.assertEquals(10, res);
    }

    @Test
    void boolFalse() {
        var res = (Boolean) interpreter.visit(false);
        Assertions.assertFalse(res);
    }

    @Test
    void boolTrue() {
        var res = (Boolean) interpreter.visit(true);
        Assertions.assertTrue(res);
    }

    @Test
    void objectEmpty() {
        var res = interpreter.visit(object());
        Assertions.assertNotNull(res);
    }


    @Test
    void objectSingleEntryNumber() {
        var res = (ObjectLiteralPair) interpreter.visit(object("env", number(2)));
        Assertions.assertNotNull(res);
        Assertions.assertEquals("env", res.key());
        Assertions.assertEquals(2, res.value());
    }

    @Test
    void objectSingleEntryBoolean() {
        var res = (ObjectLiteralPair) interpreter.visit(object("env", bool(true)));
        Assertions.assertNotNull(res);
        Assertions.assertEquals("env", res.key());
        Assertions.assertEquals(true, res.value());
    }

    @Test
    void NullTest() {
        var res = (NullValue) interpreter.visit(new SymbolIdentifier());
        Assertions.assertEquals(NullValue.of(), res.getRuntimeValue());
    }
}
