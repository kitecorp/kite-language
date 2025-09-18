package io.kite.Frontend.Parse;

import io.kite.Frontend.Parser.ParserErrors;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.Identifier.id;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.kite.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.kite.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.kite.Frontend.Parser.Expressions.ValDeclaration.val;
import static io.kite.Frontend.Parser.Program.program;
import static io.kite.Frontend.Parser.Statements.ValStatement.valStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
@DisplayName("Parser val")
@Disabled("won't support val yet")
public class ValDeclarationTest extends ParserTest {

    @Test
    void testDeclaration() {
        parse("val x");
        assertTrue(ParserErrors.hadErrors());
    }

    @Test
    void testDeclarations() {
        parse("val x,y");
        assertTrue(ParserErrors.hadErrors());
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
        var res = parse("val x,y=2");
        assertEquals("val \"x\" must be initialized", ParserErrors.getErrors().getFirst().getMessage());
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
