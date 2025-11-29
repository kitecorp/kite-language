package cloud.kitelang.syntax.parser;

import cloud.kitelang.syntax.ast.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Program.program;
import static cloud.kitelang.syntax.ast.expressions.ArrayExpression.array;
import static cloud.kitelang.syntax.ast.expressions.ObjectExpression.objectExpression;
import static cloud.kitelang.syntax.ast.expressions.ValDeclaration.val;
import static cloud.kitelang.syntax.ast.statements.ValStatement.valStatement;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.ObjectLiteral.object;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Log4j2
@DisplayName("Parser val")
@Disabled("won't support val yet")
public class ValDeclarationTest extends ParserTest {

    @Test
    void testDeclaration() {
        assertThrows(ValidationException.class, () -> parse("val x"));
    }

    @Test
    void testDeclarations() {
        assertThrows(ValidationException.class, () -> parse("val x,y"));
    }

    @Test
    void testDeclarationWithInit() {
        var res = parse("val x = 2");
        var expected = program(
                valStatement(
                        val("x", number(2))
                ));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testDeclarationsWithValues() {
        var err = assertThrows(ValidationException.class, () -> parse("val x,y=2"));
        assertEquals("val \"x\" must be initialized", err.getMessage());
    }

    @Test
    void testInitWithValues() {
        var res = parse("val x=3,y=2");
        var expected = program(
                valStatement(
                        val("x", number(3)),
                        val("y", number(2))
                ));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void multiVarInitWithValues() {
        var res = parse("""
                val x=3
                val y=2
                """);
        var expected = program(
                valStatement(val("x", number(3))),
                valStatement(val("y", number(2)))
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void multiVarInitWithValuesAndLineterminator() {
        var res = parse("""
                val x=3;
                val y=2;
                """);
        var expected = program(
                valStatement(val("x", number(3))),
                valStatement(val("y", number(2)))
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void arrayOfVar() {
        var res = parse("""
                val x=3;
                val y=2.2;
                val z=[x,y];
                """);
        var expected = program(
                valStatement(val("x", 3)),
                valStatement(val("y", 2.2)),
                valStatement(val("z", array(id("x"), id("y"))))
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
                val x=3;
                val y=2.2;
                val s="s";
                val z=[x,y,s,5];
                """);
        var expected = program(
                valStatement(val("x", 3)),
                valStatement(val("y", 2.2)),
                valStatement(val("s", "s")),
                valStatement(val("z", array(
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
                val x=3;
                val y={ env: "prod" };
                val s="s";
                val z=[x,y,s,5];
                """);
        var expected = program(
                valStatement(val("x", 3)),
                valStatement(val("y", objectExpression(object("env", "prod")))),
                valStatement(val("s", "s")),
                valStatement(val("z", array(
                                id("x"), id("y"), id("s"), number(5))
                        )
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

}
