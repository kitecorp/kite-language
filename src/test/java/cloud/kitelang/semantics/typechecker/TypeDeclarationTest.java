package cloud.kitelang.semantics.typechecker;

import cloud.kitelang.syntax.parser.ParserTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Program.program;
import static cloud.kitelang.syntax.ast.expressions.VarDeclaration.var;
import static cloud.kitelang.syntax.ast.statements.VarStatement.statement;
import static cloud.kitelang.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Parser Type")
public class TypeDeclarationTest extends ParserTest {

    @Test
    void testNumber() {
        var res = parse("var number x");
        var expected = program(statement(var("x", type("number"))));
        assertEquals(expected, res);
    }

    @Test
    void testBoolean() {
        var res = parse("var boolean x");
        var expected = program(statement(var("x", type("boolean"))));
        assertEquals(expected, res);
    }

    @Test
    void testString() {
        var res = parse("var string x ");
        var expected = program(statement(var("x", type("string"))));
        assertEquals(expected, res);
    }

    @Test
    void testCustom() {
        var res = parse("var Subnet x");
        var expected = program(statement(var("x", type("Subnet"))));
        assertEquals(expected, res);
    }

    @Test
    void testCustomPath() {
        var res = parse("var Aws.Networking.Subnet x");
        var expected = program(statement(var("x", type("Aws.Networking.Subnet"))));
        assertEquals(expected, res);
    }


}
