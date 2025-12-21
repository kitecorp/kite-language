package cloud.kitelang.syntax.parser;

import cloud.kitelang.semantics.types.ValueType;
import cloud.kitelang.syntax.ast.statements.ExpressionStatement;
import cloud.kitelang.syntax.literals.TypeIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cloud.kitelang.syntax.ast.Program.program;
import static cloud.kitelang.syntax.ast.expressions.BinaryExpression.binary;
import static cloud.kitelang.syntax.ast.statements.BlockExpression.block;
import static cloud.kitelang.syntax.ast.statements.FunctionDeclaration.fun;
import static cloud.kitelang.syntax.ast.statements.ReturnStatement.funReturn;
import static cloud.kitelang.syntax.literals.ParameterIdentifier.param;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
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
    }

    @Test
    void testEmptyBody() {
        var res = parse("""
                fun square() { 
                }
                """);
        var expected = program(fun("square", block()));
        assertEquals(expected, res);
    }


}
