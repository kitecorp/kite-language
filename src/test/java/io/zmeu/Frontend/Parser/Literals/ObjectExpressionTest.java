package io.zmeu.Frontend.Parser.Literals;

import io.zmeu.Frontend.Parse.ParserTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.zmeu.Frontend.Parser.Expressions.VarDeclaration.var;
import static io.zmeu.Frontend.Parser.Literals.BooleanLiteral.bool;
import static io.zmeu.Frontend.Parser.Literals.Identifier.id;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Literals.ObjectLiteral.object;
import static io.zmeu.Frontend.Parser.Literals.StringLiteral.string;
import static io.zmeu.Frontend.Parser.Program.program;
import static io.zmeu.Frontend.Parser.Statements.VarStatement.varStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser object")
public class ObjectExpressionTest extends ParserTest {

    @Test
    void varEmptyObject() {
        var res = parse("var x = {}");
        var expected = program(varStatement(var(id("x"), objectExpression())));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToNumber() {
        var res = parse("var x = { a: 2}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object(id("a"), number(2))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToDecimal() {
        var res = parse("var x = { a: 2.2}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object(id("a"), number(2.2))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToTrue() {
        var res = parse("var x = { a:true}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object(id("a"), bool(true))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToFalse() {
        var res = parse("var x = { a:false}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object(id("a"), bool(false))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToNull() {
        var res = parse("var x = { a:null}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object(id("a"), NullLiteral.nullLiteral())))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToNesteObject() {
        var res = parse("""
                var x = { 
                    a: { 
                        b: 2
                    }
                }
                
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(object(id("a"),
                        objectExpression(object(id("b"), number(2)))
                )))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToString() {
        var res = parse("""
                var x = { a: "2"}
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(object(id("a"), string("2"))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToString2() {
        var res = parse("""
                var x = { a: "hello"}
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(object(id("a"), string("hello"))))));
        assertEquals(expected, res);
        log.info((res));
    }


}
