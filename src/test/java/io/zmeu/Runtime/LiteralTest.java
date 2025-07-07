package io.zmeu.Runtime;

import io.zmeu.Base.RuntimeTest;
import io.zmeu.Frontend.Parser.Literals.SymbolIdentifier;
import io.zmeu.Runtime.Values.NullValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Literals.BooleanLiteral.bool;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Literals.ObjectLiteral.ObjectLiteralPair;
import static io.zmeu.Frontend.Parser.Literals.ObjectLiteral.object;
import static io.zmeu.Frontend.Parser.Literals.StringLiteral.string;

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
    void stringLiteral() {
        var res = interpreter.visit("""
                "hello world!"
                """);
        Assertions.assertEquals("\"hello world!\"\n", res);
    }

    @Test
    void stringLiterals() {
        var res = interpreter.visit("hello world!");
        Assertions.assertEquals("hello world!", res);
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
    void objectSingleEntryString() {
        var res = (ObjectLiteralPair) interpreter.visit(object("env", string("production")));
        Assertions.assertNotNull(res);
        Assertions.assertEquals("env", res.key());
        Assertions.assertEquals("production", res.value());
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
