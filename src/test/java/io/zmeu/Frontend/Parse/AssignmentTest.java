package io.zmeu.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.zmeu.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.zmeu.Frontend.Parser.Literals.Identifier.id;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.of;
import static io.zmeu.Frontend.Parser.Program.program;
import static io.zmeu.Frontend.Parser.Statements.BlockExpression.block;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Assignment")
public class AssignmentTest extends BaseTest {

    @Test
    void testAssignment() {
        var res = parse("x=2");
        var expected = program(expressionStatement(assign("=", id("x"), of(2))));
        assertEquals(expected, res);
        log.warn(toJson(res));
    }

    @Test
    void testAssignmentBlock() {
        var res = parse("x={2}");
        var expected = program(expressionStatement(assign("=", id("x"), block(expressionStatement(of(2))))));
        assertEquals(expected, res);
        log.warn(toJson(res));
    }

    @Test
    void testAssignmentBlockWithStatements() {
        var res = parse("""
                x={
                    y=2
                    2
                }
                """);
        var expected = program(expressionStatement(
                assign("=", id("x"),
                        block(expressionStatement(assign("=", id("y"), number(2))),
                                expressionStatement(number(2))))));
        log.warn(toJson(res));
        assertEquals(expected, res);
    }

    @Test
    void testMultipleAssignments() {
        var res = parse("x=y=2");
        var expected = program(expressionStatement(
                assign("=", id("x"),
                        assign("=", id("y"), number(2))
                )));
        assertEquals(expected, res);
        log.warn(toJson(res));
    }

    @Test
    void testMultipleAssignments3() {
        var res = parse("x=y=z=2");
        var expected = program(
                expressionStatement(
                        assign("=", id("x"),
                                assign("=", id("y"),
                                        assign("=", id("z"), number(2)))
                        )
                )
        );
        assertEquals(expected, res);
        log.warn(toJson(res));
    }

    @Test
    void testVariableAddition() {
        var res = parse("x+x");
        var expected = program(
                expressionStatement(
                        binary(id("x"), id("x"), "+")
                )
        );
        assertEquals(expected, res);
        log.warn(toJson(res));
    }

    @Test
    void testAssignmentOnlyToIdentifiers() {
        parse("2=2");
    }

    @Test
    void assignInvalid() {
        parse("1+2=10");
    }


    @Test
    void assignInvalidMember() {
        parse("a().x+1=10");
    }


}
