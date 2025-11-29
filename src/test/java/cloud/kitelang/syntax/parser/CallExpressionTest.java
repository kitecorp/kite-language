package cloud.kitelang.syntax.parser;

import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.syntax.ast.expressions.CallExpression;
import cloud.kitelang.syntax.ast.expressions.MemberExpression;
import cloud.kitelang.syntax.ast.statements.ExpressionStatement;
import cloud.kitelang.syntax.literals.Identifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Function Call")
public class CallExpressionTest extends ParserTest {

    @Test
    void testFunctionCall() {
        var res = parse("foo(x)");
        var expected = Program.of(ExpressionStatement.expressionStatement(
                CallExpression.call("foo", "x")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testFunctionCallNumber() {
        var res = parse("foo(2)");
        var expected = Program.of(ExpressionStatement.expressionStatement(
                CallExpression.call("foo", 2)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testFunctionCallDecimal() {
        var res = parse("foo(2.2)");
        var expected = Program.of(ExpressionStatement.expressionStatement(
                CallExpression.call("foo", 2.2)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void functionMultipleArgs() {
        var res = parse("foo(x,y)");
        var expected = Program.of(ExpressionStatement.expressionStatement(
                CallExpression.call("foo", "x","y")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void functionMultipleCalls() {
        var res = parse("foo(x)()");
        var expected = Program.of(ExpressionStatement.expressionStatement(
                CallExpression.call(CallExpression.call("foo", "x"), Collections.emptyList())));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void functionMultipleCallsArgs() {
        var res = parse("foo(x)(y)");
        var expected = Program.of(ExpressionStatement.expressionStatement(
                CallExpression.call(CallExpression.call("foo", "x"), "y")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testMemberAssignment() {
        var res = parse("console.log(x,y)");
        var expected = Program.of(ExpressionStatement.expressionStatement(
                CallExpression.call(
                        MemberExpression.member(false,"console", "log"),
                        Identifier.id("x", "y"))
        ));

        assertEquals(expected, res);
        log.info(res);
    }


}
