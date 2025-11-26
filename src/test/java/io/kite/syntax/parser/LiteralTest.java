package io.kite.syntax.parser;

import io.kite.syntax.ast.Program;
import io.kite.syntax.ast.expressions.StringInterpolation;
import io.kite.syntax.ast.statements.ExpressionStatement;
import io.kite.syntax.literals.NumberLiteral;
import io.kite.syntax.literals.SymbolIdentifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.syntax.ast.expressions.ArrayExpression.array;
import static io.kite.syntax.ast.expressions.ObjectExpression.objectExpression;
import static io.kite.syntax.ast.statements.ExpressionStatement.expressionStatement;
import static io.kite.syntax.literals.NumberLiteral.number;
import static io.kite.syntax.literals.ObjectLiteral.object;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Literal")
public class LiteralTest extends ParserTest {

    @Test
    void testInteger() {
        var res = parse("1");
        var expected = Program.of(expressionStatement(NumberLiteral.number(1)));
        assertEquals(expected, res);
    }

    @Test
    void testDecimal() {
        var res = parse("1.11");
        var expected = Program.of(expressionStatement(NumberLiteral.number(1.11)));
        assertEquals(expected, res);
    }

    @Test
    void testStringStatements() {
        var res = parse("""
                "Hello"
                """);
        var expected = Program.of(expressionStatement("Hello"));
        assertEquals(expected, res);
    }

    @Test
    void testMultipleStatements() {
        var res = parse("""
                "Hello"
                1
                """);
        var expected = Program.of(
                expressionStatement("Hello"),
                expressionStatement(1)
        );
        assertEquals(expected, res);
    }


    @Test
    void testIntegerStrShouldEvalToString() {
        var res = parse(""" 
                "42" 
                """);
        var expected = Program.of(
                expressionStatement("42")
        );
        assertEquals(expected, res);
    }

    @Test
    void testSingleQuotesShouldEvalToString() {
        var res = parse(""" 
                '42' 
                """);
        var expected = Program.of(
                expressionStatement("42")
        );
        assertEquals(expected, res);
    }

    @Test
    void testSingleQuotesWithSpaceShouldEvalToString() {
        var res = parse(""" 
                '  42  ' 
                """);
        var expected = Program.of(
                expressionStatement("  42  ")
        );
        assertEquals(expected, res);
    }

    @Test
    void testInterpolationChar() {
        var res = parse("""
                "$i"
                """);
        // With grammar-level interpolation, "$i" is now parsed as StringInterpolation
        var statement = (ExpressionStatement) res.getBody().get(0);
        var interpolation = (StringInterpolation) statement.getStatement();
        assertEquals(1, interpolation.getParts().size());
        var part = interpolation.getParts().get(0);
        assertEquals(StringInterpolation.Expr.class, part.getClass());
        var expr = (StringInterpolation.Expr) part;
        var identifier = (SymbolIdentifier) expr.expression();
        assertEquals("i", identifier.string());
    }

    @Test
    void testNumberStringShouldEvalToNumber() {
        var res = parse("42");
        var expected = Program.of(
                expressionStatement(42)
        );
        assertEquals(expected, res);
    }

    @Test
    void testSimpleObject() {
        var res = (Program) parse("{ a: 2}");
        var expected = Program.of(
                expressionStatement(objectExpression(object("a", number(2)))
                ));
        assertEquals(expected, res);
        log.info(printer.visit(res));
    }

    @Test
    void testSimpleArray() {
        var res = (Program) parse("[]");
        var expected = Program.of(
                expressionStatement(array())
        );
        assertEquals(expected, res);
        log.info(printer.visit(res));
    }

    @Test
    void testSimpleArrayWithNumbers() {
        var res = (Program) parse("[1,2,3]");
        var expected = Program.of(
                expressionStatement(array(1, 2, 3))
        );
        assertEquals(expected, res);
        log.info(printer.visit(res));
    }

    @Test
    void testSimpleArrayWithDecimals() {
        var res = (Program) parse("[1.0,2.4,3.5]");
        var expected = Program.of(
                expressionStatement(array(1.0, 2.4, 3.5))
        );
        assertEquals(expected, res);
        log.info(printer.visit(res));
    }

    @Test
    void testSimpleArrayWithStrings() {
        var res = (Program) parse("['a','b','c']");
        var expected = Program.of(
                expressionStatement(array("a", "b", "c"))
        );
        assertEquals(expected, res);
        log.info(printer.visit(res));
    }

    @Test
    void testSimpleArrayWithStringsDoubleQuotes() {
        var res = (Program) parse("""
                ["a","b","c"]
                """);
        var expected = Program.of(
                expressionStatement(array("a", "b", "c"))
        );
        assertEquals(expected, res);
        log.info(printer.visit(res));
    }

    @Test
    void testSimpleArrayWithBooleans() {
        var res = (Program) parse("[true,true,true]");
        var expected = Program.of(
                expressionStatement(array(true, true, true))
        );
        assertEquals(expected, res);
        log.info(printer.visit(res));
    }

    @Test
    void testSimpleArrayWithBooleansMixed() {
        var res = (Program) parse("[true,false,true]");
        var expected = Program.of(
                expressionStatement(array(true, false, true))
        );
        assertEquals(expected, res);
        log.info(printer.visit(res));
    }

    @Test
    void testNumberStringShouldEvalToNumberWithTrailingSpace() {
        var res = parse("   \"  42  \"    ");
        var expected = Program.of(
                expressionStatement("  42  ")
        );
        assertEquals(expected, res);
    }

}
