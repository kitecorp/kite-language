package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parser.Literals.NumberLiteral;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.ExpressionStatement;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Literal")
public class LiteralTest extends ParserTest {

    @Test
    void testInteger() {
        var res = parse("1");
        var expected = Program.of(ExpressionStatement.expressionStatement(NumberLiteral.of(1)));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testDecimal() {
        var res = parse("1.11");
        var expected = Program.of(ExpressionStatement.expressionStatement(NumberLiteral.of(1.11)));
        assertEquals(expected, res);
    }

    @Test
    void testStringStatements() {
        var res = parse("""
                "Hello"
                """);
        var expected = Program.of(
                ExpressionStatement.expressionStatement("Hello")
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
                ExpressionStatement.expressionStatement("Hello"),
                ExpressionStatement.expressionStatement(1)
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
                ExpressionStatement.expressionStatement("42")
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
                ExpressionStatement.expressionStatement("42")
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
                ExpressionStatement.expressionStatement("  42  ")
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testNumberStringShouldEvalToNumber() {
        var res = parse("42");
        var expected = Program.of(
                ExpressionStatement.expressionStatement(42)
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testNumberStringShouldEvalToNumberWithTrailingSpace() {
        var res = parse("   \"  42  \"    ");
        var expected = Program.of(
                ExpressionStatement.expressionStatement("  42  ")
        );
        assertEquals(expected, res);
        log.info((res));
    }

}
