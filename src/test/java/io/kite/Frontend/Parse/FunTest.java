package io.kite.Frontend.Parse;

import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Statements.ExpressionStatement;
import io.kite.TypeChecker.Types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.kite.Frontend.Parse.Literals.ParameterIdentifier.param;
import static io.kite.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.kite.Frontend.Parser.Program.program;
import static io.kite.Frontend.Parser.Statements.BlockExpression.block;
import static io.kite.Frontend.Parser.Statements.FunctionDeclaration.fun;
import static io.kite.Frontend.Parser.Statements.ReturnStatement.funReturn;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Function")
public class FunTest extends ParserTest {

    @Test
    void testWithArgs() {
        var res = parse("""
                fun square(number x) { 
                    return x*x
                }
                """);
        var expected = program(
                fun("square", List.of(param("x", "number")), block(
                                funReturn(binary("*", "x", "x"))
                        )
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testWith2Args() {
        var res = parse("""
                fun square(number x,number y) { 
                    return x*y
                }
                """);
        var expected = program(
                fun("square", List.of(param("x", "number"), param("y", "number")), block(
                                funReturn(
                                        binary("*", "x", "y")
                                )
                        )
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testWithoutReturn() {
        var res = parse("""
                fun square(number x) { 
                    return
                }
                """);
        var expected = program(
                fun("square", List.of(param("x","number")), block(
                                funReturn(ExpressionStatement.expressionStatement(TypeIdentifier.type(ValueType.Void)))
                        )
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testWithoutParamsAndReturn() {
        var res = parse("""
                fun square() { 
                    return
                }
                """);
        var expected = program(
                fun("square", block(
                                funReturn(ExpressionStatement.expressionStatement(TypeIdentifier.type(ValueType.Void)))
                        )
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testEmptyBody() {
        var res = parse("""
                fun square() { 
                }
                """);
        var expected = program(fun("square", block()));
        assertEquals(expected, res);
        log.info(res);
    }


}
