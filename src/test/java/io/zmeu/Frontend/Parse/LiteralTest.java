package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parse.Literals.NumberLiteral;
import io.zmeu.Frontend.Parser.Program;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parse.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.zmeu.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.zmeu.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Literal")
public class LiteralTest extends ParserTest {

    @Test
    void testInteger() {
        var res = parse("1");
        var expected = Program.of(expressionStatement(NumberLiteral.of(1)));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testDecimal() {
        var res = parse("1.11");
        var expected = Program.of(expressionStatement(NumberLiteral.of(1.11)));
        assertEquals(expected, res);
    }

    @Test
    void testStringStatements() {
        var res = parse("""
                "Hello"
                """);
        var expected = Program.of(
                expressionStatement("Hello")
        );
        assertEquals(expected, res);
        log.info((res));
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
        log.info((res));
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
        log.info((res));
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
        log.info((res));
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
        log.info((res));
    }

    @Test
    void testNumberStringShouldEvalToNumber() {
        var res = parse("42");
        var expected = Program.of(
                expressionStatement(42)
        );
        assertEquals(expected, res);
        log.info((res));
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
    void testSimpleArrayWithUmbers() {
        var res = (Program) parse("[1,2,3]");
        var expected = Program.of(
                expressionStatement(array(1, 2, 3))
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
        log.info((res));
    }

}
