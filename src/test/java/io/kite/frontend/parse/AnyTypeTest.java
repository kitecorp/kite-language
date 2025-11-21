package io.kite.frontend.parse;

import io.kite.frontend.parse.literals.ArrayTypeIdentifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.frontend.parse.literals.BooleanLiteral.bool;
import static io.kite.frontend.parse.literals.NullLiteral.nullLiteral;
import static io.kite.frontend.parse.literals.NumberLiteral.number;
import static io.kite.frontend.parse.literals.ObjectLiteral.object;
import static io.kite.frontend.parse.literals.StringLiteral.string;
import static io.kite.frontend.parse.literals.TypeIdentifier.type;
import static io.kite.frontend.parser.Program.program;
import static io.kite.frontend.parser.expressions.ArrayExpression.array;
import static io.kite.frontend.parser.expressions.ObjectExpression.objectExpression;
import static io.kite.frontend.parser.expressions.VarDeclaration.var;
import static io.kite.frontend.parser.statements.VarStatement.varStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser any type")
public class AnyTypeTest extends ParserTest {

    @Test
    void anyNumber() {
        var res = parse("var any x = 1");
        var expected = program(varStatement(var("x", type("any"), 1)));
        assertEquals(expected, res);
    }

    @Test
    void anyDecimal() {
        var res = parse("var any x = 1.1");
        var expected = program(varStatement(var("x", type("any"), number(1.1))));
        assertEquals(expected, res);
    }


    @Test
    void anyTrue() {
        var res = parse("var any x = true");
        var expected = program(varStatement(var("x", type("any"), bool(true))));
        assertEquals(expected, res);
    }

    @Test
    void anyFalse() {
        var res = parse("var any x = false");
        var expected = program(varStatement(var("x", type("any"), bool(false))));
        assertEquals(expected, res);
    }

    @Test
    void anyString() {
        var res = parse("var any x = 'hello'");
        var expected = program(varStatement(var("x", type("any"), string("hello"))));
        assertEquals(expected, res);
    }

    @Test
    void anyNull() {
        var res = parse("var any x = null");
        var expected = program(varStatement(var("x", type("any"), nullLiteral())));
        assertEquals(expected, res);
    }

    @Test
    void anyObject() {
        var res = parse("var any x = {}");
        var expected = program(varStatement(var("x", type("any"), objectExpression())));
        assertEquals(expected, res);
    }

    @Test
    void anyObjectEnv() {
        var res = parse("var any x = { env: 'dev'}");
        var expected = program(varStatement(var("x", type("any"), objectExpression(object("env", "dev")))));
        assertEquals(expected, res);
    }

    @Test
    void anyArray() {
        var res = parse("var any[] x = [1,2,3]");
        var expected = program(varStatement(var("x", new ArrayTypeIdentifier(type("any")), array(1, 2, 3))));
        assertEquals(expected, res);
    }


}
