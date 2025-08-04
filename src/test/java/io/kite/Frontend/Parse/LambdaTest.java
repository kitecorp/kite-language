package io.kite.Frontend.Parse;

import io.kite.TypeChecker.Types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.kite.Frontend.Parse.Literals.ParameterIdentifier.param;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;
import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.kite.Frontend.Parser.Expressions.CallExpression.call;
import static io.kite.Frontend.Parser.Program.program;
import static io.kite.Frontend.Parser.Statements.BlockExpression.block;
import static io.kite.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static io.kite.Frontend.Parser.Statements.LambdaExpression.lambda;
import static io.kite.Frontend.Parser.Statements.ReturnStatement.funReturn;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Lambda")
public class LambdaTest extends ParserTest {

    @Test
    void lambdaSimple() {
        var res = parse("(number x) -> x*x");
        var expected = program(expressionStatement(
                        lambda(param("x", type("number")), binary("*", "x", "x"))
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void lambdaArgTypeWithReturnType() {
        var res = parse("(number x ) number -> x*x");
        var expected = program(expressionStatement(
                        lambda(param("x", type(ValueType.Number)), binary("*", "x", "x"), type(ValueType.Number))
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void lambdaNoReturn() {
        var res = parse("""
                (number x ) -> {
                    print(x)
                }
                """);
        var expected = program(expressionStatement(
                        lambda(param("x", type(ValueType.Number)),
                                block(
                                        expressionStatement(
                                                call("print", "x")
                                        )
                                ))
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void lambdaTwoArgs() {
        var res = parse("(number x,number y) -> x*y");
        var expected = program(
                expressionStatement(
                        lambda(param("x", "number"),
                                param("y", "number"), binary("*", "x", "y")))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void lambdaBlock() {
        var res = parse("(number x, number y) -> { x*y }");
        var expected = program(
                expressionStatement(
                        lambda(param("x", "number"),
                                param("y", "number"),
                                block(expressionStatement(binary("*", "x", "y"))))
                ));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testWith2Args() {
        var res = parse("""
                (number x) -> { 
                    return x*x
                }
                """);
        var expected = program(expressionStatement(lambda(param("x", "number"), block(
                        funReturn(binary("*", "x", "x"))
                )
        )));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testWithoutReturn() {
        var res = parse("""
                (object x) -> { 
                    return
                }
                """);
        var expected = program(expressionStatement(lambda(param("x", "object"), block(
                        funReturn(expressionStatement(type(ValueType.Void)))
                )
        )));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testWithoutParamsAndReturn() {
        var res = parse("""
                () -> { 
                }
                """);
        var expected = program(expressionStatement(lambda(List.of(), block())));
        assertEquals(expected, res);
        log.warn((res));
    }

    @Test
    void callExpression() {
        var res = parse("""
                ((number x) -> x*x)(2) 
                
                """);
        var expected = program(
                expressionStatement(call(lambda(param("x", "number"), binary("*", "x", "x")), 2)));
        log.warn((res));
        assertEquals(expected, res);
    }

    @Test
    void callExpressionEmpty() {
        var res = parse("""
                ((number x) -> x*x)(2)()
                
                """);
        var expected = program(
                expressionStatement(
                        call(call(lambda(param("x","number"), binary("*", "x", "x")), 2), Collections.emptyList())
                ));
        log.warn((res));
        assertEquals(expected, res);
    }

    @Test
    void callExpressionHi() {
        var res = parse("""
                ((number x) -> x*x)(2)("hi")
                """);
        var expected = program(
                expressionStatement(
                        call(call(lambda(param("x","number"), binary("*", "x", "x")), 2), string("hi"))
                ));
        log.warn((res));
        assertEquals(expected, res);
    }

}
