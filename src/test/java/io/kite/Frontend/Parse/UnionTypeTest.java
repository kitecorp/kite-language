package io.kite.Frontend.Parse;

import io.kite.Frontend.Parse.Literals.NullLiteral;
import io.kite.ParserErrors;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.BooleanLiteral.bool;
import static io.kite.Frontend.Parse.Literals.Identifier.symbol;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;
import static io.kite.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.kite.Frontend.Parser.Expressions.UnionTypeStatement.type;
import static io.kite.Frontend.Parser.Program.program;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Type alias")
public class UnionTypeTest extends ParserTest {

    @Test
    void typeDeclarationNumber() {
        var res = parse("type int = 1");
        var expected = program(type("int", number(1)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationDecimal() {
        var res = parse("type int = 1.1");
        var expected = program(type("int", number(1.1)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationTrue() {
        var res = parse("type bool = true");
        var expected = program(type("bool", bool(true)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationFalse() {
        var res = parse("type bool = false");
        var expected = program(type("bool", bool(false)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationString() {
        var res = parse("type hey = 'hello'");
        var expected = program(type("hey", string("hello")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationNull() {
        var res = parse("type nil = null");
        var expected = program(type("nil", NullLiteral.nullLiteral()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationObject() {
        var res = parse("type hey = { env: 'dev' }");
        var expected = program(type("hey", objectExpression(object("env", "dev"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationEmptyObject() {
        var res = parse("type hey = { }");
        var expected = program(type("hey", objectExpression()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationAnotherType() {
        var res = parse("""
                type int = 1
                type INT = int
                """);
        var expected = program(type("int", number(1)), type("INT", symbol("int")));
        assertEquals(expected, res);
        log.info(res);
    }


    @Test
    void typeDeclarationUnionNumberError() {
        var res = parse("type int = 1 | 1");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
        log.info(res);
    }

    @Test
    void typeUnionNumbers() {
        var res = parse("type ints = 1 | 2");
        var expected = program(type("ints", number(1), number(2)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeUnionDecimals() {
        var res = parse("type ints = 1.2 | 2.2");
        var expected = program(type("ints", number(1.2), number(2.2)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeUnionBooleans() {
        var res = parse("type booleans = true | false");
        var expected = program(type("booleans", bool(true), bool(false)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeUnionStrings() {
        var res = parse("type hey = 'hello' | \"world\"  ");
        var expected = program(type("hey", string("hello"), string("world")));
        assertEquals(expected, res);
        log.info(res);
    }


    @Test
    void typeUnionObjects() {
        var res = parse("type hey = { env: 'dev' } | { env: 'prod' }");
        var expected = program(type("hey", objectExpression(object("env", "dev")), objectExpression(object("env", "prod"))));
        assertEquals(expected, res);
        log.info(res);
    }


    @Test
    void typeUnionAnotherTypes() {
        var res = parse("""
                type one = 1
                type two = 2
                type INT = one | two
                """);
        var expected = program(
                type("one", number(1)),
                type("two", number(2)),
                type("INT", symbol("one"), symbol("two")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeUnionAnotherExisting() {
        var res = parse("""
                type bool = boolean
                """);
        var expected = program(type("bool", symbol("boolean")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeUnionAnotherMixedTypes() {
        var res = parse("""
                type zero = 0
                type INT = 1 | 'hello' | true | { env: 'dev' } | zero 
                """);
        var expected = program(
                type("zero", number(0)),
                type("INT", number(1), string("hello"), bool(true), objectExpression(object("env", "dev")), symbol("zero")));
        assertEquals(expected, res);
        log.info(res);
    }


}
