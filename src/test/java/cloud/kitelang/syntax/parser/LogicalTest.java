package cloud.kitelang.syntax.parser;

import cloud.kitelang.syntax.literals.Identifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Program.program;
import static cloud.kitelang.syntax.ast.expressions.AssignmentExpression.assignment;
import static cloud.kitelang.syntax.ast.expressions.BinaryExpression.binary;
import static cloud.kitelang.syntax.ast.expressions.LogicalExpression.logical;
import static cloud.kitelang.syntax.ast.statements.ExpressionStatement.expressionStatement;
import static cloud.kitelang.syntax.literals.BooleanLiteral.bool;
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
