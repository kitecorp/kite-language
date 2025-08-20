package io.kite.Frontend.Parse;

import io.kite.Frontend.Parse.Literals.BooleanLiteral;
import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.TypeExpression;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.kite.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.kite.Frontend.Parser.Expressions.TypeExpression.type;
import static io.kite.Frontend.Parser.Program.program;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Type alias")
public class TypeTest extends ParserTest {

    @Test
    void typeDeclarationNumber() {
        var res = parse("type int = 1");
        var expected = program(type("int", number(1)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationDecimal() {
        var res = parse("type int = 1.1");
        var expected = program(type("int", number(1.1)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationTrue() {
        var res = parse("type bool = true");
        var expected = program(type("bool", BooleanLiteral.bool(true)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationFalse() {
        var res = parse("type bool = false");
        var expected = program(type("bool", BooleanLiteral.bool(false)));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationString() {
        var res = parse("type hey = 'hello'");
        var expected = program(type("hey", StringLiteral.string("hello")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationObject() {
        var res = parse("type hey = { env: 'dev' }");
        var expected = program(type("hey", objectExpression(object("env", "dev"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void typeDeclarationEmptyObject() {
        var res = parse("type hey = { }");
        var expected = program(type("hey", objectExpression()));
        assertEquals(expected, res);
        log.info(res);
    }


}
