package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parser.Literals.Identifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Expressions.AssignmentExpression.assignment;
import static io.zmeu.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.zmeu.Frontend.Parser.Expressions.Expressions.logical;
import static io.zmeu.Frontend.Parser.Literals.BooleanLiteral.bool;
import static io.zmeu.Frontend.Parser.Program.program;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Logical")
public class LogicalTest extends BaseTest {

    @Test
    void testLogicalAnd() {
        var res = parse("x > 0 && y < 0");
        var expected = program(expressionStatement(
                logical("&&",
                        binary("x", 0, ">"),
                        binary("y", 0, "<")
                )
        ));
        log.info(toJson(res));
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
        log.info(toJson(res));
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
        log.info(toJson(res));
        assertEquals(expected, res);
    }

    @Test
    void testLogicalOrEquals() {
        var res = parse("x = true || false");
        var expected = program(expressionStatement(
                assignment("=", Identifier.id("x"),
                        logical("||", bool(true), bool(false))
                )));
        log.info(toJson(res));
        assertEquals(expected, res);
    }

    @Test
    void testLogicalAndEquals() {
        var res = parse("x = true && false");
        var expected = program(expressionStatement(
                assignment("=", Identifier.id("x"),
                        logical("&&", bool(true), bool(false))
                )));
        log.info(toJson(res));
        assertEquals(expected, res);
    }

}
