package io.zmeu.Runtime;

import io.zmeu.Base.RuntimeTest;
import io.zmeu.Frontend.Parser.Literals.ObjectLiteral;
import io.zmeu.Frontend.Parser.Literals.SymbolIdentifier;
import io.zmeu.Runtime.Values.NullValue;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Literals.BooleanLiteral.bool;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
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
        var res = interpreter.visit(ObjectLiteral.object());
        Assertions.assertNotNull(res);
    }

    @Test
    void objectSingleEntryString() {
        var res = (Pair) interpreter.visit(ObjectLiteral.object("env", string("production")));
        Assertions.assertNotNull(res);
        Assertions.assertEquals("env", res.getLeft());
        Assertions.assertEquals("production", res.getRight());
    }

    @Test
    void objectSingleEntryNumber() {
        var res = (Pair) interpreter.visit(ObjectLiteral.object("env", number(2)));
        Assertions.assertNotNull(res);
        Assertions.assertEquals("env", res.getLeft());
        Assertions.assertEquals(2, res.getRight());
    }

    @Test
    void objectSingleEntryBoolean() {
        var res = (Pair) interpreter.visit(ObjectLiteral.object("env", bool(true)));
        Assertions.assertNotNull(res);
        Assertions.assertEquals("env", res.getLeft());
        Assertions.assertEquals(true, res.getRight());
    }

    @Test
    void NullTest() {
        var res = (NullValue) interpreter.visit(new SymbolIdentifier());
        Assertions.assertEquals(NullValue.of(), res.getRuntimeValue());
    }
}
