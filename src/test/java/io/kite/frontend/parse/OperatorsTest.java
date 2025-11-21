package io.kite.frontend.parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.frontend.parse.literals.NumberLiteral.number;
import static io.kite.frontend.parser.Program.program;
import static io.kite.frontend.parser.expressions.BinaryExpression.binary;
import static io.kite.frontend.parser.statements.ExpressionStatement.expressionStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Operators")
public class OperatorsTest extends ParserTest {


    @Test
    void testAddition() {
        var res = parse("1 + 1");

        var expected = program(expressionStatement(binary(1, 1, "+")));
        assertEquals(expected, res);
    }

    @Test
    void testAdditionMultipleLines() {
        var res = parse("""
                1 + 1
                2+2
                """);

        var expected = program(
                expressionStatement(
                        binary(1, 1, "+")),
                expressionStatement(
                        binary(2, 2, "+")));
        assertEquals(expected, res);
    }

    @Test
    void testSubstraction() {
        var res = parse("1-1");

        var expected = program(expressionStatement(binary(1, 1, "-")));
        assertEquals(expected, res);
    }

    @Test
    void testMultiplication() {
        var res = parse("1*1");

        var expected = program(expressionStatement(binary(1, 1, "*")));
        assertEquals(expected, res);
    }

    @Test
    void testDivision() {
        var res = parse("1/1");

        var expected = program(expressionStatement(binary(1, 1, "/")));
        assertEquals(expected, res);
    }

    @Test
    void testAddition3() {
        var res = parse("1 + 1+1");
        var expected = program(expressionStatement(
                binary(
                        binary(1, 1, "+"),
                        number(1),
                        "+"))
        );
        assertEquals(expected, res);
    }

    @Test
    void testAdditionSubstraction3() {
        var res = parse("1 + 1-11");
        var expected = program(expressionStatement(
                binary(
                        binary(1, 1, "+"),
                        11,
                        "-"))
        );
        assertEquals(expected, res);
    }

    @Test
    void testAdditionMultiplication() {
        var res = parse("1 + 2*3");
        var expected = program(
                expressionStatement(
                        binary(
                                1,
                                binary(2, 3, "*"),
                                "+"))
        );
        assertEquals(expected, res);
    }

    @Test
    void testAdditionMultiplicationChangeOrder() {
        var res = parse("(1 + 2) * 3");
        var expected = program(
                expressionStatement(
                        binary(binary(1, 2, "+"), 3, "*")
                ));

        assertEquals(expected, res);
    }

    @Test
    void testAdditionMultiplicationChangeOrder2() {
        var res = parse("3 * (1 + 2)");
        var expected = program(
                expressionStatement(
                        binary(3,
                                binary(1, 2, "+"),
                                "*")
                ));

        assertEquals(expected, res);
    }

    @Test
    void testAdditionParanthesis() {
        var res = parse("1 + 2 - (3*4)");
        var expected = program(
                expressionStatement(
                        binary(
                                binary(1, 2, "+"),
                                binary(3, 4, "*"),
                                "-"))
        );
        assertEquals(expected, res);
    }


    @Test
    void testMultiplicationWithParanthesis() {
        var res = parse("1 * 2 - (3*4)");
        var expected = program(expressionStatement(
                binary(binary(1, 2, "*"), binary(3, 4, "*"), "-"))
        );
        assertEquals(expected, res);
    }

    @Test
    void testAddSubWithParenthesis() {
        var res = parse("(1+2 + (3-4))");
        var expected = program(
                expressionStatement(
                        binary(
                                binary(1, 2, "+"),
                                binary(3, 4, "-"),
                                "+"))
        );
        assertEquals(expected, res);
    }

    @Test
    void testDivisionWithParanthesis() {
        var res = parse("1 / 2 - (3/4)");
        var expected = program(
                expressionStatement(
                        binary(
                                binary(1, 2, "/"),
                                binary(3, 4, "/"),
                                "-"))
        );
        assertEquals(expected, res);
    }


}
