package cloud.kitelang.syntax.parser;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Program.program;
import static cloud.kitelang.syntax.ast.expressions.ArrayExpression.array;
import static cloud.kitelang.syntax.ast.expressions.ObjectExpression.objectExpression;
import static cloud.kitelang.syntax.ast.expressions.VarDeclaration.var;
import static cloud.kitelang.syntax.ast.statements.VarStatement.statement;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.ObjectLiteral.object;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class ArrayTest extends ParserTest {


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
        log.info(res);
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
        log.info(res);
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
        log.info(res);
    }
}
