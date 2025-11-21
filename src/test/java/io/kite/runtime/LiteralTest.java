package io.kite.runtime;

import io.kite.base.RuntimeTest;
import io.kite.frontend.parse.literals.SymbolIdentifier;
import io.kite.runtime.values.NullValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.kite.frontend.parse.literals.BooleanLiteral.bool;
import static io.kite.frontend.parse.literals.NumberLiteral.number;
import static io.kite.frontend.parse.literals.ObjectLiteral.ObjectLiteralPair;
import static io.kite.frontend.parse.literals.ObjectLiteral.object;

@Log4j2
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
