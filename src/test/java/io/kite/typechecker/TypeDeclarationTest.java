package io.kite.typechecker;

import io.kite.frontend.parse.ParserTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.frontend.parse.literals.TypeIdentifier.type;
import static io.kite.frontend.parser.Program.program;
import static io.kite.frontend.parser.expressions.VarDeclaration.var;
import static io.kite.frontend.parser.statements.VarStatement.statement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Type")
public class TypeDeclarationTest extends ParserTest {

    @Test
    void testNumber() {
        var res = parse("var number x");
        var expected = program(statement(var("x", type("number"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testBoolean() {
        var res = parse("var boolean x");
        var expected = program(statement(var("x", type("boolean"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testString() {
        var res = parse("var string x ");
        var expected = program(statement(var("x", type("string"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testCustom() {
        var res = parse("var Subnet x");
        var expected = program(statement(var("x", type("Subnet"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testCustomPath() {
        var res = parse("var Aws.Networking.Subnet x");
        var expected = program(statement(var("x", type("Aws.Networking.Subnet"))));
        assertEquals(expected, res);
        log.info(res);
    }


}
