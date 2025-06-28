package io.zmeu.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Unary")
public class UnaryTest extends BaseTest {

    @Test
    void testLogicalUnary() {
        var res = parse("-x");
        var expected = program(unary("-", "x"));
        log.info(toJson(res));
        assertEquals(expected, res);
    }

    @Test
    void testLogicalNot() {
        var res = parse("!x");
        var expected = program(unary("!", "x"));
        log.info(toJson(res));
        assertEquals(expected, res);
    }

    @Test
    void prefixDecrement() {
        var res = parse("--x");
        var expected = program(unary("--", "x"));
        log.info(toJson(res));
        assertEquals(expected, res);
    }

//    @Test
//    void postfixDecrement() {
//        var res = parse("x--");
//        var expected = program()(
//                unary()("--", "x"))
//        );
//        log.info(toJson(res));
//        assertEquals(expected, res);
//    }

    @Test
    void prefixIncrement() {
        var res = parse("++x");
        var expected = program(unary("++", "x"));
        log.info(toJson(res));
        assertEquals(expected, res);
    }

//    @Test
//    void postfixIncrement() {
//        var res = parse("x++");
//        var expected = program()(
//                unary()("++", "x")
//        ));
//        log.info(toJson(res));
//        assertEquals(expected, res);
//    }

    @Test
    void testLogicalUnaryHigherPrecedenceThanMultiplication() {
        var res = parse("-x * 2");
        var expected = program(binary("*", unary("-", "x"), 2));

        log.info(toJson(res));
        assertEquals(expected, res);
    }


}
