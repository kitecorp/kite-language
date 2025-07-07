package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parse.Literals.TypeIdentifier;
import io.zmeu.Frontend.Parser.Statements.ExpressionStatement;
import io.zmeu.TypeChecker.Types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.zmeu.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.zmeu.Frontend.Parse.Literals.ParameterIdentifier.param;
import static io.zmeu.Frontend.Parser.Program.program;
import static io.zmeu.Frontend.Parser.Statements.BlockExpression.block;
import static io.zmeu.Frontend.Parser.Statements.FunctionDeclaration.fun;
import static io.zmeu.Frontend.Parser.Statements.ReturnStatement.funReturn;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Function")
public class FunTest extends ParserTest {

    @Test
    void testWithArgs() {
        var res = parse("""
                fun square(x) { 
                    return x*x
                }
                """);
        var expected = program(
                fun("square", List.of(param("x")), block(
                                funReturn(binary("*", "x", "x"))
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testWith2Args() {
        var res = parse("""
                fun square(x,y) { 
                    return x*y
                }
                """);
        var expected = program(
                fun("square", List.of(param("x"), param("y")), block(
                                funReturn(
                                        binary("*", "x", "y")
                                )
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testWithoutReturn() {
        var res = parse("""
                fun square(x) { 
                    return
                }
                """);
        var expected = program(
                fun("square", List.of(param("x")), block(
                                funReturn(ExpressionStatement.expressionStatement(TypeIdentifier.type(ValueType.Void)))
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
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
        log.info((res));
    }

    @Test
    void testEmptyBody() {
        var res = parse("""
                fun square() { 
                }
                """);
        var expected = program(fun("square", block()));
        assertEquals(expected, res);
        log.info((res));
    }


}
