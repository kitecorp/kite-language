package io.kite.syntax.parser;

import io.kite.syntax.ast.expressions.UnionTypeStatement;
import io.kite.syntax.parser.literals.NullLiteral;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.syntax.ast.Program.program;
import static io.kite.syntax.ast.expressions.ObjectExpression.objectExpression;
import static io.kite.syntax.ast.expressions.UnionTypeStatement.union;
import static io.kite.syntax.parser.literals.BooleanLiteral.bool;
import static io.kite.syntax.parser.literals.Identifier.id;
import static io.kite.syntax.parser.literals.NumberLiteral.number;
import static io.kite.syntax.parser.literals.ObjectLiteral.object;
import static io.kite.syntax.parser.literals.StringLiteral.string;
import static io.kite.syntax.parser.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Type alias")
public class UnionTypeTest extends ParserTest {

    @Test
    void typeDeclarationNumber() {
        var res = parse("type int = 1");
        var expected = program(union("int", number(1)));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationDecimal() {
        var res = parse("type int = 1.1");
        var expected = program(union("int", number(1.1)));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationTrue() {
        var res = parse("type bool = true");
        var expected = program(union("bool", bool(true)));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationFalse() {
        var res = parse("type bool = false");
        var expected = program(union("bool", bool(false)));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationString() {
        var res = parse("type hey = 'hello'");
        var expected = program(union("hey", string("hello")));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationNull() {
        var res = parse("type nil = null");
        var expected = program(union("nil", NullLiteral.nullLiteral()));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationObject() {
        var res = parse("type hey = { env: 'dev' }");
        var expected = program(union("hey", objectExpression(object("env", "dev"))));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationEmptyObject() {
        var res = parse("type hey = { }");
        var expected = program(union("hey", objectExpression()));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationAnyObject() {
        var res = parse("type hey = object");
        var expected = program(union("hey", type("object")));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationAnyObjectEmpty() {
        var res = parse("type hey = object()");
        var expected = program(union("hey", objectExpression()));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationAnyObjectCurlyEmpty() {
        var res = parse("type hey = object()");
        var expected = program(union("hey", objectExpression()));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationEmptyObjectLiteral() {
        var res = parse("type hey = {}");
        var expected = program(union("hey", objectExpression()));
        assertEquals(expected, res);
    }

    @Test
    void typeDeclarationAnotherType() {
        var res = parse("""
                type int = 1
                type INT = int
                """);
        var expected = program(union("int", number(1)), union("INT", id("int")));
        assertEquals(expected, res);
    }

    @Test
    void typeUnionNumbers() {
        var res = parse("type ints = 1 | 2");
        var expected = program(UnionTypeStatement.union("ints", number(1), number(2)));
        assertEquals(expected, res);
    }

    @Test
    void typeUnionDecimals() {
        var res = parse("type ints = 1.2 | 2.2");
        var expected = program(UnionTypeStatement.union("ints", number(1.2), number(2.2)));
        assertEquals(expected, res);
    }

    @Test
    void typeUnionBooleans() {
        var res = parse("type booleans = true | false");
        var expected = program(UnionTypeStatement.union("booleans", bool(true), bool(false)));
        assertEquals(expected, res);
    }

    @Test
    void typeUnionStrings() {
        var res = parse("type hey = 'hello' | \"world\"  ");
        var expected = program(UnionTypeStatement.union("hey", string("hello"), string("world")));
        assertEquals(expected, res);
    }


    @Test
    void typeUnionObjects() {
        var res = parse("type hey = { env: 'dev' } | { env: 'prod' }");
        var expected = program(UnionTypeStatement.union("hey", objectExpression(object("env", "dev")), objectExpression(object("env", "prod"))));
        assertEquals(expected, res);
    }


    @Test
    void typeUnionAnotherTypes() {
        var res = parse("""
                type one = 1
                type two = 2
                type INT = one | two
                """);
        var expected = program(
                union("one", number(1)),
                union("two", number(2)),
                UnionTypeStatement.union("INT", id("one"), id("two")));
        assertEquals(expected, res);
    }

    @Test
    void typeUnionAnotherExisting() {
        var res = parse("""
                type bool = boolean
                """);
        var expected = program(union("bool", id("boolean")));
        assertEquals(expected, res);
    }

    @Test
    void typeUnionAnotherMixedTypes() {
        var res = parse("""
                type zero = 0
                type INT = 1 | 'hello' | true | { env: 'dev' } | zero 
                """);
        var expected = program(
                union("zero", number(0)),
                UnionTypeStatement.union("INT", number(1), string("hello"), bool(true), objectExpression(object("env", "dev")), id("zero")));
        assertEquals(expected, res);
    }


}
