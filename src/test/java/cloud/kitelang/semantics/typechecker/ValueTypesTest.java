package cloud.kitelang.semantics.typechecker;

import cloud.kitelang.syntax.ast.Factory;
import cloud.kitelang.syntax.parser.ParserTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Factory.program;
import static cloud.kitelang.syntax.ast.expressions.VarDeclaration.var;
import static cloud.kitelang.syntax.ast.statements.VarStatement.varStatement;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.StringLiteral.string;
import static cloud.kitelang.syntax.literals.TypeIdentifier.id;
import static cloud.kitelang.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("TypeChecker Type")
public class ValueTypesTest extends ParserTest {

    @Test
    void testString() {
        var res = parse("var string x\n");
        var expected = program(varStatement(var("x", type("string"))));
        assertEquals(expected, res);
    }

    @Test
    void testStringEOF() {
        var res = parse("var string x ");
        var expected = program(varStatement(var("x", type("string"))));
        assertEquals(expected, res);
    }

    @Test
    void testNumber() {
        var res = parse("var number x ");
        var expected = program(varStatement(var("x", type("number"))));
        assertEquals(expected, res);
    }

    @Test
    void testObjectEOF() {
        var res = parse("var object x ");
        var expected = program(varStatement(var("x", type("object"))));
        assertEquals(expected, res);
    }

    @Test
    void testStringLineTerminator() {
        var res = parse("var string x ;");
        var expected = program(varStatement(var("x", type("string"))));
        assertEquals(expected, res);
    }

    @Test
    void testStringInit() {
        var res = parse("""
                var string x = "test"
                """);
        var expected = program(var(id("x"), type("string"), string("test")));
        assertEquals(expected, res);
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
        log.info("{}", actual);
    }

    @Test
    void testNumberFromStdInit() {
        var actual = parse("""
                var std.Number x =2
                """);
        var expected = program(var(id("x"), type("std.Number"), number(2)));
        assertEquals(expected, actual);
        log.info("{}", actual);
    }

    @Test
    void testSpace() {
        var actual = parse("""
                var std.Number       x=2
                """);
        var expected = program(var(id("x"), type("std.Number"), number(2)));
        assertEquals(expected, actual);
        log.info("{}", actual);
    }


}
