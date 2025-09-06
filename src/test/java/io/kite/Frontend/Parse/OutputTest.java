package io.kite.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.ArrayTypeIdentifier.arrayType;
import static io.kite.Frontend.Parse.Literals.Identifier.symbol;
import static io.kite.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.kite.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.kite.Frontend.Parser.Expressions.OutputDeclaration.output;
import static io.kite.Frontend.Parser.Expressions.UnionTypeStatement.union;
import static io.kite.Frontend.Parser.Factory.program;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Outputs")
public class OutputTest extends ParserTest {

    @Test
    void outputString() {
        var res = parse("output string something");
        var expected = program(output("something", type("string")));
        assertEquals(expected, res);
    }

    @Test
    void outputNumber() {
        var res = parse("output number something");
        var expected = program(output("something", type("number")));
        assertEquals(expected, res);
    }

    @Test
    void outputBoolean() {
        var res = parse("output boolean something");
        var expected = program(output("something", type("boolean")));
        assertEquals(expected, res);
    }

    @Test
    void outputObject() {
        var res = parse("output object something");
        var expected = program(output("something", type("object")));
        assertEquals(expected, res);
    }

    @Test
    void outputUnion() {
        var res = parse("""
                type custom = string | number
                output custom something""");
        var expected = program(
                union("custom", symbol("string"), symbol("number")),
                output("something", type("custom"))
        );
        assertEquals(expected, res);
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
                union("custom", symbol("string"), symbol("number")),
                output("something", type("custom"), 10)
        );
        assertEquals(expected, res);
    }

    @Test
    void outputStringArray() {
        var res = parse("output string[] something");
        var expected = program(output("something", arrayType("string")));
        assertEquals(expected, res);
    }

    @Test
    void outputNumberArray() {
        var res = parse("output number[] something");
        var expected = program(output("something", arrayType("number")));
        assertEquals(expected, res);
    }

    @Test
    void outputBooleanArray() {
        var res = parse("output boolean[] something");
        var expected = program(output("something", arrayType("boolean")));
        assertEquals(expected, res);
    }

    @Test
    void outputObjectArray() {
        var res = parse("output object[] something");
        var expected = program(output("something", arrayType("object")));
        assertEquals(expected, res);
    }

    @Test
    void outputUnionArray() {
        var res = parse("""
                type custom = string | number
                output custom[] something
                """);
        var expected = program(
                union("custom", symbol("string"), symbol("number")),
                output("something", arrayType("custom")
                ));
        assertEquals(expected, res);
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
                union("custom", symbol("string"), symbol("number")),
                output("something", arrayType("custom"), array(10)
                ));
        assertEquals(expected, res);
    }
}
