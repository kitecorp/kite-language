package cloud.kitelang.syntax.parser;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Unary")
public class UnaryTest extends ParserTest {

    @Test
    void testLogicalUnary() {
        var res = parse("-x");
        var expected = program(unary("-", "x"));
        assertEquals(expected, res);
    }

    @Test
    void testLogicalNot() {
        var res = parse("!x");
        var expected = program(unary("!", "x"));
        assertEquals(expected, res);
    }

    @Test
    void prefixDecrement() {
        var res = parse("--x");
        var expected = program(unary("--", "x"));
        assertEquals(expected, res);
    }

//    @Test
//    void postfixDecrement() {
//        var res = parse("x--");
//        var expected = program()(
//                unary()("--", "x"))
//        );
//        assertEquals(expected, res);
//    }

    @Test
    void prefixIncrement() {
        var res = parse("++x");
        var expected = program(unary("++", "x"));
        assertEquals(expected, res);
    }

//    @Test
//    void postfixIncrement() {
//        var res = parse("x++");
//        var expected = program()(
//                unary()("++", "x")
//        ));
//        assertEquals(expected, res);
//    }

    @Test
    void testLogicalUnaryHigherPrecedenceThanMultiplication() {
        var res = parse("-x * 2");
        var expected = program(binary("*", unary("-", "x"), 2));

        assertEquals(expected, res);
    }


}
