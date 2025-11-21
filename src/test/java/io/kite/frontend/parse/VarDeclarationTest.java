package io.kite.frontend.parse;

import io.kite.frontend.parse.literals.StringLiteral;
import io.kite.frontend.parser.Program;
import io.kite.frontend.parser.expressions.AssignmentExpression;
import io.kite.frontend.parser.statements.VarStatement;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.frontend.parse.literals.BooleanLiteral.bool;
import static io.kite.frontend.parse.literals.NullLiteral.nullLiteral;
import static io.kite.frontend.parser.Program.program;
import static io.kite.frontend.parser.expressions.VarDeclaration.var;
import static io.kite.frontend.parser.statements.ExpressionStatement.expressionStatement;
import static io.kite.frontend.parser.statements.VarStatement.statement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Var")
public class VarDeclarationTest extends ParserTest {

    @Test
    void testDeclaration() {
        var res = parse("var x");
        var expected = program(statement(var("x")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testDeclarations() {
        var res = parse("var x,y");
        var expected = program(
                statement(
                        var("x"),
                        var("y")
                ));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testDeclarationWithInit() {
        var res = parse("var x = 2");
        var expected = program(
                statement(
                        var("x", 2)
                ));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testDeclarationsWithValues() {
        var res = parse("var x,y=2");
        var expected = program(
                statement(
                        var("x"),
                        var("y", 2)
                ));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testInitWithValues() {
        var res = parse("var x=3,y=2");
        var expected = program(
                statement(
                        var("x", 3),
                        var("y", 2)
                ));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void multiVarInitNumbers() {
        var res = parse("""
                var x=3
                var y=2
                """);
        var expected = program(
                statement(var("x", 3)),
                statement(var("y", 2))
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void multiVarBooleans() {
        var res = parse("""
                var x=true
                var y=false
                """);
        var expected = program(
                statement(var("x", bool(true))),
                statement(var("y", bool(false)))
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void multiVarNull() {
        var res = parse("""
                var x=null
                """);
        var expected = program(
                statement(var("x", nullLiteral()))
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void multiVarInitWithValuesAndLineterminator() {
        var res = parse("""
                var x=3;
                var y=2;
                """);
        var expected = program(
                statement(var("x", 3)),
                statement(var("y", 2))
        );
        assertEquals(expected, res);
        log.info(res);
    }

    /**
     * Works during parsing. Type checker should throw exception
     */
    @Test
    void reassignWithInvalidValue() {
        var res = parse("""
                var x=3;
                x="hello"
                """);
        var expected = program(
                statement(var("x", 3)),
                expressionStatement(AssignmentExpression.assign("x", "hello"))
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void interpolation() {
        var res = (Program) parse("""
                var x="world";
                var y="hello $x";
                """);
        var expected = program(
                statement(var("x", "world")),
                statement(var("y", "hello $x"))
        );
        assertEquals(expected, res);
        var statement = (VarStatement) res.getBody().get(1);
        var x = statement.getDeclarations().get(0);
        var literal = (StringLiteral) x.getInit();
        Assertions.assertTrue(literal.isInterpolated());
        Assertions.assertEquals("x", literal.getInterpolationVars().get(0));
        log.info(res);
    }


}
