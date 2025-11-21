package io.kite.syntax.parser;

import io.kite.syntax.parser.literals.ArrayTypeIdentifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.syntax.ast.Program.program;
import static io.kite.syntax.ast.expressions.ArrayExpression.array;
import static io.kite.syntax.ast.expressions.ObjectExpression.objectExpression;
import static io.kite.syntax.ast.expressions.VarDeclaration.var;
import static io.kite.syntax.ast.statements.VarStatement.varStatement;
import static io.kite.syntax.parser.literals.BooleanLiteral.bool;
import static io.kite.syntax.parser.literals.NullLiteral.nullLiteral;
import static io.kite.syntax.parser.literals.NumberLiteral.number;
import static io.kite.syntax.parser.literals.ObjectLiteral.object;
import static io.kite.syntax.parser.literals.StringLiteral.string;
import static io.kite.syntax.parser.literals.TypeIdentifier.type;
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
