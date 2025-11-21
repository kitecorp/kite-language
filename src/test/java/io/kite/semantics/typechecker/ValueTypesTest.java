package io.kite.semantics;

import io.kite.syntax.ast.Factory;
import io.kite.syntax.parser.ParserTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.syntax.ast.Factory.program;
import static io.kite.syntax.ast.expressions.VarDeclaration.var;
import static io.kite.syntax.ast.statements.VarStatement.varStatement;
import static io.kite.syntax.literals.NumberLiteral.number;
import static io.kite.syntax.literals.StringLiteral.string;
import static io.kite.syntax.literals.TypeIdentifier.id;
import static io.kite.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("TypeChecker Type")
public class ValueTypesTest extends ParserTest {

    @Test
    void testString() {
        var res = parse("var string x\n");
        var expected = program(varStatement(var("x", type("string"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testStringEOF() {
        var res = parse("var string x ");
        var expected = program(varStatement(var("x", type("string"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testNumber() {
        var res = parse("var number x ");
        var expected = program(varStatement(var("x", type("number"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testObjectEOF() {
        var res = parse("var object x ");
        var expected = program(varStatement(var("x", type("object"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testStringLineTerminator() {
        var res = parse("var string x ;");
        var expected = program(varStatement(var("x", type("string"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testStringInit() {
        var res = parse("""
                var string x = "test"
                """);
        var expected = program(var(id("x"), type("string"), string("test")));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testNumberInitDouble() {
        parse("""
                var number x = 0.2
                """);
    }

    @Test
    void testNumberFromStd() {
        var actual = parse("""
                var std.Number x 
                """);
        var expected = program(Factory.var(id("x"), type("std.Number")));
        assertEquals(expected, actual);
        log.info((actual));
    }

    @Test
    void testNumberFromStdInit() {
        var actual = parse("""
                var std.Number x =2
                """);
        var expected = program(var(id("x"), type("std.Number"), number(2)));
        assertEquals(expected, actual);
        log.info((actual));
    }

    @Test
    void testSpace() {
        var actual = parse("""
                var std.Number       x=2
                """);
        var expected = program(var(id("x"), type("std.Number"), number(2)));
        assertEquals(expected, actual);
        log.info((actual));
    }


}
