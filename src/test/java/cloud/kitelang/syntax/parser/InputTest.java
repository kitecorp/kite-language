package cloud.kitelang.syntax.parser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Factory.program;
import static cloud.kitelang.syntax.ast.expressions.ArrayExpression.array;
import static cloud.kitelang.syntax.ast.expressions.InputDeclaration.input;
import static cloud.kitelang.syntax.ast.expressions.ObjectExpression.objectExpression;
import static cloud.kitelang.syntax.ast.expressions.UnionTypeStatement.union;
import static cloud.kitelang.syntax.literals.ArrayTypeIdentifier.arrayType;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.ObjectLiteral.object;
import static cloud.kitelang.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Parser Inputs")
public class InputTest extends ParserTest {

    @Test
    void inputString() {
        var res = parse("input string something");
        var expected = program(input("something", type("string")));
        assertEquals(expected, res);
    }

    @Test
    void inputNumber() {
        var res = parse("input number something");
        var expected = program(input("something", type("number")));
        assertEquals(expected, res);
    }

    @Test
    void inputBoolean() {
        var res = parse("input boolean something");
        var expected = program(input("something", type("boolean")));
        assertEquals(expected, res);
    }

    @Test
    void inputObject() {
        var res = parse("input object something");
        var expected = program(input("something", type("object")));
        assertEquals(expected, res);
    }

    @Test
    void inputUnion() {
        var res = parse("""
                type custom = string | number
                input custom something""");
        var expected = program(
                union("custom", id("string"), id("number")),
                input("something", type("custom"))
        );
        assertEquals(expected, res);
    }

    @Test
    void inputStringInit() {
        var res = parse("input string something = 'something'");
        var expected = program(input("something", type("string"), "something"));
        assertEquals(expected, res);
    }

    @Test
    void inputNumberInit() {
        var res = parse("input number something = 10 ");
        var expected = program(input("something", type("number"), 10));
        assertEquals(expected, res);
    }

    @Test
    void inputBooleanInit() {
        var res = parse("input boolean something = true");
        var expected = program(input("something", type("boolean"), true));
        assertEquals(expected, res);
    }

    @Test
    void inputObjectInitEmpty() {
        var res = parse("input object something = {}");
        var expected = program(input("something", type("object"), objectExpression()));
        assertEquals(expected, res);
    }

    @Test
    void inputObjectInit() {
        var res = parse("input object something = {env : 'dev'}");
        var expected = program(input("something", type("object"), objectExpression(object("env", "dev"))));
        assertEquals(expected, res);
    }

    @Test
    void inputUnionInit() {
        var res = parse("""
                type custom = string | number
                input custom something = 10
                """);
        var expected = program(
                union("custom", id("string"), id("number")),
                input("something", type("custom"), 10)
        );
        assertEquals(expected, res);
    }

    @Test
    void inputStringArray() {
        var res = parse("input string[] something");
        var expected = program(input("something", arrayType("string")));
        assertEquals(expected, res);
    }

    @Test
    void inputNumberArray() {
        var res = parse("input number[] something");
        var expected = program(input("something", arrayType("number")));
        assertEquals(expected, res);
    }

    @Test
    void inputBooleanArray() {
        var res = parse("input boolean[] something");
        var expected = program(input("something", arrayType("boolean")));
        assertEquals(expected, res);
    }

    @Test
    void inputObjectArray() {
        var res = parse("input object[] something");
        var expected = program(input("something", arrayType("object")));
        assertEquals(expected, res);
    }

    @Test
    void inputUnionArray() {
        var res = parse("""
                type custom = string | number
                input custom[] something
                """);
        var expected = program(
                union("custom", id("string"), id("number")),
                input("something", arrayType("custom")
                ));
        assertEquals(expected, res);
    }

    @Test
    void inputStringArrayInit() {
        var res = parse("input string[] something=['hi']");
        var expected = program(input("something", arrayType("string"), array("hi")));
        assertEquals(expected, res);
    }

    @Test
    void inputNumberArrayInit() {
        var res = parse("input number[] something=[1,2,3]");
        var expected = program(input("something", arrayType("number"), array(1, 2, 3)));
        assertEquals(expected, res);
    }

    @Test
    void inputBooleanArrayInit() {
        var res = parse("input boolean[] something=[true,false,true]");
        var expected = program(input("something", arrayType("boolean"), array(true, false, true)));
        assertEquals(expected, res);
    }

    @Test
    void inputObjectArrayInit() {
        var res = parse("input object[] something=[{env:'dev'}]");
        var expected = program(input("something", arrayType("object"), array(objectExpression(object("env", "dev")))));
        assertEquals(expected, res);
    }


    @Test
    void inputUnionArrayInit() {
        var res = parse("""
                type custom = string | number
                input custom[] something = [10]
                """);
        var expected = program(
                union("custom", id("string"), id("number")),
                input("something", arrayType("custom"), array(10)
                ));
        assertEquals(expected, res);
    }
}
