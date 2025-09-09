package io.kite.Frontend.Parse;

import io.kite.Frontend.Parser.ParserErrors;
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
        parse("output string something");
        assertEquals("Missing '=' after: output string something",ParserErrors.errors());
    }

    @Test
    void outputNumber() {
        parse("output number something");
        assertEquals("Missing '=' after: output number something",ParserErrors.errors());
    }

    @Test
    void outputBoolean() {
        parse("output boolean something");
        assertEquals("Missing '=' after: output boolean something",ParserErrors.errors());
    }

    @Test
    void outputObject() {
        parse("output object something");
        assertEquals("Missing '=' after: output object something",ParserErrors.errors());
    }

    @Test
    void outputUnion() {
        var res = parse("""
                type custom = string | number
                output custom something""");
        assertEquals("Missing '=' after: output custom something",ParserErrors.errors());
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
        parse("output string[] something");
        assertEquals("Missing '=' after: output string[] something",ParserErrors.errors());
    }

    @Test
    void outputNumberArray() {
        parse("output number[] something");
        assertEquals("Missing '=' after: output number[] something",ParserErrors.errors());
    }

    @Test
    void outputBooleanArray() {
        parse("output boolean[] something");
        assertEquals("Missing '=' after: output boolean[] something",ParserErrors.errors());
    }

    @Test
    void outputObjectArray() {
        parse("output object[] something");
        assertEquals("Missing '=' after: output object[] something",ParserErrors.errors());
    }

    @Test
    void outputUnionArray() {
        parse("""
                type custom = string | number
                output custom[] something
                """);
        assertEquals("Missing '=' after: output custom[] something",ParserErrors.errors());
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
