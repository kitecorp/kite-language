package cloud.kitelang.syntax.parser;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Program.program;
import static cloud.kitelang.syntax.ast.expressions.AssignmentExpression.assign;
import static cloud.kitelang.syntax.ast.expressions.BinaryExpression.binary;
import static cloud.kitelang.syntax.ast.statements.ExpressionStatement.expressionStatement;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.NumberLiteral.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Assignment")
public class AssignmentTest extends ParserTest {

    @Test
    void testAssignment() {
        var res = parse("x=2");
        var expected = program(expressionStatement(assign("=", id("x"), of(2))));
        assertEquals(expected, res);
    }

//    @Test
//    void testAssignmentBlock() {
//        var res = parse("x={2}");
//        var expected = program(expressionStatement(assign("=", id("x"), block(expressionStatement(of(2))))));
//        assertEquals(expected, res);
//    }

//    @Test
//    void testAssignmentBlockWithStatements() {
//        var res = parse("""
//                x={
//                    y=2
//                    2
//                }
//                """);
//        var expected = program(expressionStatement(
//                assign("=", id("x"),
//                        block(expressionStatement(assign("=", id("y"), number(2))),
//                                expressionStatement(number(2))))));
//        assertEquals(expected, res);
//    }

    @Test
    void testMultipleAssignments() {
        var res = parse("x=y=2");
        var expected = program(expressionStatement(
                assign("=", id("x"),
                        assign("=", id("y"), number(2))
                )));
        assertEquals(expected, res);
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
