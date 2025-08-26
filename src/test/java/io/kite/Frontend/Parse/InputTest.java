package io.kite.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.InputDeclaration.input;
import static io.kite.Frontend.Parser.Factory.program;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
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




}
