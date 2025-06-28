package io.zmeu.Frontend.Parse;

import io.zmeu.ErrorSystem;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Expressions.ValDeclaration.val;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Program.program;
import static io.zmeu.Frontend.Parser.Statements.BlockExpression.block;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static io.zmeu.Frontend.Parser.Statements.ValStatement.valStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
@DisplayName("Parser val")
public class ValDeclarationTest extends ParserTest {

    @Test
    void testDeclaration() {
        parse("val x");
        assertTrue(ErrorSystem.hadErrors());
    }

    @Test
    void testDeclarations() {
        parse("val x,y");
        assertTrue(ErrorSystem.hadErrors());
    }

    @Test
    void testDeclarationWithInit() {
        var res = parse("val x = 2");
        var expected = program(
                valStatement(
                        val("x", number(2))
                ));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testDeclarationsWithValues() {
        var res = parse("val x,y=2");
        assertEquals("val \"x\" must be initialized", ErrorSystem.getErrors().getFirst().getMessage());
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
        log.info((res));
    }


    @Test
    void initVarWithBlockStatement() {
        var res = parse("""
                val x={
                    2
                }
                """);
        var expected = program(valStatement(
                val("x", block(expressionStatement(number(2)))))
        );
        log.warn((res));
        assertEquals(expected, res);
    }

    @Test
    void testAssignmentBlockWithStatements() {
        var res = parse("""
                val x={
                    val y=2
                    3
                }
                """);
        var expected = program(
                valStatement(
                        val("x", block(
                                valStatement(val("y", number(2))),
                                expressionStatement(number(3)))))
        );
        log.warn((res));
        assertEquals(expected, res);
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
        log.info((res));
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
        log.info((res));
    }


}
