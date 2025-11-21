package io.kite.frontend.parse;

import io.kite.frontend.parse.literals.Identifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.frontend.parse.literals.BooleanLiteral.bool;
import static io.kite.frontend.parser.Program.program;
import static io.kite.frontend.parser.expressions.AssignmentExpression.assignment;
import static io.kite.frontend.parser.expressions.BinaryExpression.binary;
import static io.kite.frontend.parser.expressions.Expressions.logical;
import static io.kite.frontend.parser.statements.ExpressionStatement.expressionStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Logical")
public class LogicalTest extends ParserTest {

    @Test
    void testLogicalAnd() {
        var res = parse("x > 0 && y < 0");
        var expected = program(expressionStatement(
                logical("&&",
                        binary("x", 0, ">"),
                        binary("y", 0, "<")
                )
        ));
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void testLogicalOr() {
        var res = parse("x > 0 || y < 0");
        var expected = program(expressionStatement(
                logical("||",
                        binary("x", 0, ">"),
                        binary("y", 0, "<")
                )
        ));
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void testLogical() {
        var res = parse("x > 0 || y < 0 && z < 0");
        var expected = program(expressionStatement(
                logical("||",
                        binary(">", "x", 0),
                        logical("&&",
                                binary("<", "y", 0),
                                binary("<", "z", 0))
                )));
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void testLogicalOrEquals() {
        var res = parse("x = true || false");
        var expected = program(expressionStatement(
                assignment("=", Identifier.id("x"),
                        logical("||", bool(true), bool(false))
                )));
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void testLogicalAndEquals() {
        var res = parse("x = true && false");
        var expected = program(expressionStatement(
                assignment("=", Identifier.id("x"),
                        logical("&&", bool(true), bool(false))
                )));
        log.info(res);
        assertEquals(expected, res);
    }

}
