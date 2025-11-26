package io.kite.syntax.parser;

import io.kite.syntax.ast.Program;
import io.kite.syntax.ast.expressions.AssignmentExpression;
import io.kite.syntax.ast.statements.VarStatement;
import io.kite.syntax.literals.StringLiteral;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.syntax.ast.Program.program;
import static io.kite.syntax.ast.expressions.VarDeclaration.var;
import static io.kite.syntax.ast.statements.ExpressionStatement.expressionStatement;
import static io.kite.syntax.ast.statements.VarStatement.statement;
import static io.kite.syntax.literals.BooleanLiteral.bool;
import static io.kite.syntax.literals.NullLiteral.nullLiteral;
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
        // First variable is still a simple StringLiteral
        var statement1 = (VarStatement) res.getBody().get(0);
        var decl1 = statement1.getDeclarations().get(0);
        Assertions.assertInstanceOf(StringLiteral.class, decl1.getInit());

        // Second variable should be a StringInterpolation with "hello " text and $x identifier
        var statement2 = (VarStatement) res.getBody().get(1);
        var decl2 = statement2.getDeclarations().get(0);
        Assertions.assertInstanceOf(io.kite.syntax.ast.expressions.StringInterpolation.class, decl2.getInit());
        var interpolation = (io.kite.syntax.ast.expressions.StringInterpolation) decl2.getInit();
        Assertions.assertEquals(2, interpolation.getParts().size());

        // First part is text "hello "
        var textPart = (io.kite.syntax.ast.expressions.StringInterpolation.Text) interpolation.getParts().get(0);
        Assertions.assertEquals("hello ", textPart.value());

        // Second part is expression $x
        var exprPart = (io.kite.syntax.ast.expressions.StringInterpolation.Expr) interpolation.getParts().get(1);
        var identifier = (io.kite.syntax.literals.SymbolIdentifier) exprPart.expression();
        Assertions.assertEquals("x", identifier.string());

        log.info(res);
    }


}
