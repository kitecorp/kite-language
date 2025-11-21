package io.kite.syntax.parser;

import io.kite.semantics.types.ObjectType;
import io.kite.semantics.types.ValueType;
import io.kite.syntax.ast.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.syntax.ast.Factory.program;
import static io.kite.syntax.ast.expressions.ArrayExpression.array;
import static io.kite.syntax.ast.expressions.ObjectExpression.objectExpression;
import static io.kite.syntax.ast.expressions.OutputDeclaration.output;
import static io.kite.syntax.ast.expressions.UnionTypeStatement.union;
import static io.kite.syntax.ast.expressions.VarDeclaration.var;
import static io.kite.syntax.ast.statements.VarStatement.varStatement;
import static io.kite.syntax.literals.ArrayTypeIdentifier.arrayType;
import static io.kite.syntax.literals.BooleanLiteral.bool;
import static io.kite.syntax.literals.Identifier.id;
import static io.kite.syntax.literals.NumberLiteral.number;
import static io.kite.syntax.literals.ObjectLiteral.object;
import static io.kite.syntax.literals.StringLiteral.string;
import static io.kite.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Outputs")
public class OutputTest extends ParserTest {

    @Test
    void outputString() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("output string something"));
        assertEquals("Missing '=' after: output string something", err.getMessage());
    }

    @Test
    void outputNumber() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("output number something"));
        assertEquals("Missing '=' after: output number something", err.getMessage());
    }

    @Test
    void outputBoolean() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("output boolean something"));
        assertEquals("Missing '=' after: output boolean something", err.getMessage());
    }

    @Test
    void outputObject() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("output object something"));
        assertEquals("Missing '=' after: output object something", err.getMessage());
    }

    @Test
    void outputUnion() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("""
                type custom = string | number
                output custom something
                """));
        assertEquals("Missing '=' after: output custom something", err.getMessage());
    }

    @Test
    void outputStringInit() {
        var res = parse("output string something = 'something'");
        var expected = program(output("something", type("string"), "something"));
        assertEquals(expected, res);
    }

    @Test
    void outputNumberInit() {
        var res = parse("output number something = 10 ");
        var expected = program(output("something", type("number"), 10));
        assertEquals(expected, res);
    }

    @Test
    void outputBooleanInit() {
        var res = parse("output boolean something = true");
        var expected = program(output("something", type("boolean"), true));
        assertEquals(expected, res);
    }

    @Test
    void outputObjectInitEmpty() {
        var res = parse("output object something = {}");
        var expected = program(output("something", type("object"), objectExpression()));
        assertEquals(expected, res);
    }

    @Test
    void outputObjectInit() {
        var res = parse("output object something = {env : 'dev'}");
        var expected = program(output("something", type("object"), objectExpression(object("env", "dev"))));
        assertEquals(expected, res);
    }

    @Test
    void outputUnionInit() {
        var res = parse("""
                type custom = string | number
                output custom something = 10
                """);
        var expected = program(
                union("custom", id("string"), id("number")),
                output("something", type("custom"), 10)
        );
        assertEquals(expected, res);
    }

    @Test
    void outputStringArray() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("output string[] something"));
        assertEquals("Missing '=' after: output string[] something", err.getMessage());
    }

    @Test
    void outputNumberArray() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("output number[] something"));
        assertEquals("Missing '=' after: output number[] something", err.getMessage());
    }

    @Test
    void outputBooleanArray() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("output boolean[] something"));
        assertEquals("Missing '=' after: output boolean[] something", err.getMessage());
    }

    @Test
    void outputObjectArray() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("output object[] something"));
        assertEquals("Missing '=' after: output object[] something", err.getMessage());
    }

    @Test
    void outputUnionArray() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("""
                type custom = string | number
                output custom[] something
                """));
        assertEquals("Missing '=' after: output custom[] something", err.getMessage());
    }

    @Test
    void outputStringArrayInit() {
        var res = parse("output string[] something=['hi']");
        var expected = program(output("something", arrayType("string"), array("hi")));
        assertEquals(expected, res);
    }

    @Test
    void outputNumberArrayInit() {
        var res = parse("output number[] something=[1,2,3]");
        var expected = program(output("something", arrayType("number"), array(1, 2, 3)));
        assertEquals(expected, res);
    }

    @Test
    void outputBooleanArrayInit() {
        var res = parse("output boolean[] something=[true,false,true]");
        var expected = program(output("something", arrayType("boolean"), array(true, false, true)));
        assertEquals(expected, res);
    }

    @Test
    void outputObjectArrayInit() {
        var res = parse("output object[] something=[{env:'dev'}]");
        var expected = program(output("something", arrayType("object"), array(objectExpression(object("env", "dev")))));
        assertEquals(expected, res);
    }


    @Test
    void outputUnionArrayInit() {
        var res = parse("""
                type custom = string | number
                output custom[] something = [10]
                """);
        var expected = program(
                union("custom", id("string"), id("number")),
                output("something", arrayType("custom"), array(10)
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputStringVar() {
        var res = parse("""
                var x = "hello"
                output string something=x
                """);
        var expected = program(
                varStatement(var("x", string("hello"))),
                output("something", type(ValueType.String), id("x")
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputNumberVar() {
        var res = parse("""
                var x = 10
                output number something=x
                """);
        var expected = program(
                varStatement(var("x", number(10))),
                output("something", type(ValueType.Number), id("x")
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputDecimalVar() {
        var res = parse("""
                var x = 10.2
                output number something=x
                """);
        var expected = program(
                varStatement(var("x", number(10.2))),
                output("something", type(ValueType.Number), id("x")
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputDecimalZeroVar() {
        var res = parse("""
                var x = 0.1
                output number something=x
                """);
        var expected = program(
                varStatement(var("x", number(0.1))),
                output("something", type(ValueType.Number), id("x")
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputTrueVar() {
        var res = parse("""
                var x = true
                output boolean something=x
                """);
        var expected = program(
                varStatement(var("x", bool(true))),
                output("something", type(ValueType.Boolean), id("x")
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputFalseVar() {
        var res = parse("""
                var x = false
                output boolean something=x
                """);
        var expected = program(
                varStatement(var("x", bool(false))),
                output("something", type(ValueType.Boolean), id("x")
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputObjectEmptyVar() {
        var res = parse("""
                var x = {}
                output object something=x
                """);
        var expected = program(
                varStatement(var("x", objectExpression())),
                output("something", type(ObjectType.INSTANCE), id("x")
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputObjectVar() {
        var res = parse("""
                var x = { env: 'dev'}
                output object something=x
                """);
        var expected = program(
                varStatement(var("x", objectExpression(object("env", "dev")))),
                output("something", type(ObjectType.INSTANCE), id("x")
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputArrayStringVar() {
        var res = parse("""
                var x = "hello"
                output string[] something=[x]
                """);
        var expected = program(
                varStatement(var("x", string("hello"))),
                output("something", arrayType(type(ValueType.String)), array(id("x"))
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputArrayNumberVar() {
        var res = parse("""
                var x = 10
                output number[] something=[x]
                """);
        var expected = program(
                varStatement(var("x", number(10))),
                output("something", arrayType(type(ValueType.Number)), array(id("x"))
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputArrayDecimalVar() {
        var res = parse("""
                var x = 10.2
                output number[] something=[x]
                """);
        var expected = program(
                varStatement(var("x", number(10.2))),
                output("something", arrayType(type(ValueType.Number)), array(id("x"))
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputArrayDecimalZeroVar() {
        var res = parse("""
                var x = 0.1
                output number[] something=[x]
                """);
        var expected = program(
                varStatement(var("x", number(0.1))),
                output("something", arrayType(type(ValueType.Number)), array(id("x"))
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputArrayTrueVar() {
        var res = parse("""
                var x = true
                output boolean[] something=[x]
                """);
        var expected = program(
                varStatement(var("x", bool(true))),
                output("something", arrayType(type(ValueType.Boolean)), array(id("x"))
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputArrayFalseVar() {
        var res = parse("""
                var x = false
                output boolean[] something=[x]
                """);
        var expected = program(
                varStatement(var("x", bool(false))),
                output("something", arrayType(type(ValueType.Boolean)), array(id("x"))
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputArrayObjectEmptyVar() {
        var res = parse("""
                var x = {}
                output object[] something=[x]
                """);
        var expected = program(
                varStatement(var("x", objectExpression())),
                output("something", arrayType(type(ObjectType.INSTANCE)), array(id("x"))
                ));
        assertEquals(expected, res);
    }

    @Test
    void outputArrayObjectVar() {
        var res = parse("""
                var x = { env: 'dev'}
                output object[] something=[x]
                """);
        var expected = program(
                varStatement(var("x", objectExpression(object("env", "dev")))),
                output("something", arrayType(type(ObjectType.INSTANCE)), array(id("x"))
                ));
        assertEquals(expected, res);
    }

}
