package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parser.Expressions.BinaryExpression;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Expressions.LogicalExpression.and;
import static io.zmeu.Frontend.Parser.Expressions.LogicalExpression.or;
import static io.zmeu.Frontend.Parser.Factory.binary;
import static io.zmeu.Frontend.Parser.Factory.program;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Relational")
public class RelationalTest extends BaseTest {

    @Test
    void testGreaterThan() {
        var res = parse("x>2");
        var expected = program(binary("x", 2, ">"));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testGreaterThanEq() {
        var res = parse("x>=2");
        var expected = program(binary("x", 2, ">="));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testLessThan() {
        var res = parse("x<2");
        var expected = program(binary("x", 2, "<"));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testLessThanEq() {
        var res = parse("x<=2");
        var expected = program(binary("x", 2, "<="));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testLessLowerPrecedenceThanAdditive() {
        var res = parse("x+2 > 10");
        var expected = program(BinaryExpression.binary(binary("x", 2, "+"), 10, ">"));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testLessLowerPrecedenceThanAdditiveTrue() {
        var res = parse("x > 2 == true");
        var expected = program(BinaryExpression.binary(binary("x", 2, ">"), true, "=="));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testLessLowerPrecedenceThanAdditiveFalse() {
        var res = parse("x > 2 == false");
        var expected = program(BinaryExpression.binary(binary("x", 2, ">"), false, "=="));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testLessLowerPrecedenceThanAdditiveNotFalse() {
        var res = parse("x > 2 != false");
        var expected = program(BinaryExpression.binary(binary("x", 2, ">"), false, "!="));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testLessLowerPrecedenceThanAdditiveNotTrue() {
        var res = parse("x > 2 != true");
        var expected = program(BinaryExpression.binary(binary("x", 2, ">"), true, "!="));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testLogicalAnd() {
        var res = parse("x > 0 && y < 0");
        var expected = program(and(binary("x", 0, ">"), binary("y", 0, "<")));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testLogicalOr() {
        var res = parse("x > 0 || y < 0");
        var expected = program(
                or(
                        binary("x", 0, ">"),
                        binary("y", 0, "<")
                )
        );
        log.info((res));
        assertEquals(expected, res);
    }

}
