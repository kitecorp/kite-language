package io.zmeu.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Program.program;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Operators")
public class OperatorsTest extends BaseTest {


    @Test
    void testAddition() {
        var res = parser.produceAST(tokenizer.tokenize("1 + 1"));

        var expected = program(expressionStatement(binary(1, 1, "+")));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testAdditionMultipleLines() {
        var res = parser.produceAST(tokenizer.tokenize("""
                1 + 1
                2+2
                """));

        var expected = program(
                expressionStatement(
                        binary(1, 1, "+")),
                expressionStatement(
                        binary(2, 2, "+")));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testSubstraction() {
        var res = parser.produceAST(tokenizer.tokenize("1-1"));

        var expected = program(expressionStatement(binary(1, 1, "-")));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testMultiplication() {
        var res = parser.produceAST(tokenizer.tokenize("1*1"));

        var expected = program(expressionStatement(binary(1, 1, "*")));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testDivision() {
        var res = parser.produceAST(tokenizer.tokenize("1/1"));

        var expected = program(expressionStatement(binary(1, 1, "/")));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testAddition3() {
        var res = parser.produceAST(tokenizer.tokenize("1 + 1+1"));
        var expected = program(expressionStatement(
                binary(
                        binary(1, 1, "+"),
                        number(1),
                        "+"))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testAdditionSubstraction3() {
        var res = parser.produceAST(tokenizer.tokenize("1 + 1-11"));
        var expected = program(expressionStatement(
                binary(
                        binary(1, 1, "+"),
                        11,
                        "-"))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testAdditionMultiplication() {
        var res = parser.produceAST(tokenizer.tokenize("1 + 2*3"));
        var expected = program(
                expressionStatement(
                        binary(
                                1,
                                binary(2, 3, "*"),
                                "+"))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testAdditionMultiplicationChangeOrder() {
        var res = parser.produceAST(tokenizer.tokenize("(1 + 2) * 3"));
        var expected = program(
                expressionStatement(
                        binary(binary(1, 2, "+"), 3, "*")
                ));

        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testAdditionMultiplicationChangeOrder2() {
        var res = parser.produceAST(tokenizer.tokenize("3 * (1 + 2)"));
        var expected = program(
                expressionStatement(
                        binary(3,
                                binary(1, 2, "+"),
                                "*")
                ));

        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testAdditionParanthesis() {
        var res = parser.produceAST(tokenizer.tokenize("1 + 2 - (3*4)"));
        var expected = program(
                expressionStatement(
                        binary(
                                binary(1, 2, "+"),
                                binary(3, 4, "*"),
                                "-"))
        );
        assertEquals(expected, res);
        log.info((res));
    }


    @Test
    void testMultiplicationWithParanthesis() {
        var res = parser.produceAST(tokenizer.tokenize("1 * 2 - (3*4)"));
        var expected = program(expressionStatement(
                binary(binary(1, 2, "*"), binary(3, 4, "*"), "-"))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testAddSubWithParenthesis() {
        var res = parser.produceAST(tokenizer.tokenize("(1+2 + (3-4))"));
        var expected = program(
                expressionStatement(
                        binary(
                                binary(1, 2, "+"),
                                binary(3, 4, "-"),
                                "+"))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testDivisionWithParanthesis() {
        var res = parser.produceAST(tokenizer.tokenize("1 / 2 - (3/4)"));
        var expected = program(
                expressionStatement(
                        binary(
                                binary(1, 2, "/"),
                                binary(3, 4, "/"),
                                "-"))
        );
        assertEquals(expected, res);
        log.info((res));
    }


}
