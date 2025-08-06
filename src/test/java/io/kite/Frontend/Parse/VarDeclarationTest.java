package io.kite.Frontend.Parse;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.VarStatement;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.Identifier.id;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.kite.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.kite.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.kite.Frontend.Parser.Expressions.VarDeclaration.var;
import static io.kite.Frontend.Parser.Program.program;
import static io.kite.Frontend.Parser.Statements.VarStatement.statement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Var")
public class VarDeclarationTest extends ParserTest {

    @Test
    void testDeclaration() {
        var res = parse("var x");
        var expected = program(statement(var("x")));
        assertEquals(expected, res);
        log.info((res));
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
        log.info((res));
    }

    @Test
    void testDeclarationWithInit() {
        var res = parse("var x = 2");
        var expected = program(
                statement(
                        var("x", 2)
                ));
        assertEquals(expected, res);
        log.info((res));
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
        log.info((res));
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
        log.info((res));
    }

    @Test
    void multiVarInitWithValues() {
        var res = parse("""
                var x=3
                var y=2
                """);
        var expected = program(
                statement(var("x", 3)),
                statement(var("y", 2))
        );
        assertEquals(expected, res);
        log.info((res));
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
        log.info((res));
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

    @Test
    void arrayOfVar() {
        var res = parse("""
                var x=3;
                var y=2.2;
                var z=[x,y];
                """);
        var expected = program(
                statement(var("x", 3)),
                statement(var("y", 2.2)),
                statement(var("z", array(id("x"), id("y"))))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    /**
     * During parsing we don't care about wrong types being placed in the array
     */
    @Test
    void arrayOfVarMix() {
        var res = parse("""
                var x=3;
                var y=2.2;
                var s="s";
                var z=[x,y,s,5];
                """);
        var expected = program(
                statement(var("x", 3)),
                statement(var("y", 2.2)),
                statement(var("s", "s")),
                statement(var("z", array(
                                id("x"), id("y"), id("s"), number(5))
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    /**
     * During parsing we don't care about wrong types being placed in the array
     */
    @Test
    void arrayOfVarMixObject() {
        var res = parse("""
                var x=3;
                var y={ env: "prod" };
                var s="s";
                var z=[x,y,s,5];
                """);
        var expected = program(
                statement(var("x", 3)),
                statement(var("y", objectExpression(object("env", "prod")))),
                statement(var("s", "s")),
                statement(var("z", array(
                                id("x"), id("y"), id("s"), number(5))
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }


}
