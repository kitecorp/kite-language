package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Runtime.Values.NullValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class AssignmentTest extends RuntimeTest {

    private void setGlobalVar(Object of) {
        global.init("VERSION", of);
    }

    @Test
    void GlobalVarInt() {
        setGlobalVar(2);

        var res = eval("VERSION");
        assertEquals(2, res);
    }

    @Test
    void GlobalBool() {
        setGlobalVar(false);

        var res = (Boolean) eval("VERSION");
        assertFalse(res);
    }

    @Test
    void GlobalBoolTrue() {
        setGlobalVar(true);

        var res = (Boolean) eval("VERSION");
        assertTrue(res);
    }

    @Test
    void Decimal() {
        setGlobalVar(1.1);

        var res = eval("VERSION");
        assertEquals(1.1, res);
    }


    @Test
    void Null() {
        setGlobalVar(new NullValue());

        var res = eval("VERSION");
        var expected = new NullValue();
        assertEquals(expected, res);
    }

    @Test
    void AssignmentInt() {
        setGlobalVar(1);

        var res = eval("VERSION=2");
        var expected = 2;
        assertEquals(expected, res);
    }

    @Test
    void AssignmentIntSame() {
        setGlobalVar(1);

        var res = eval("VERSION=1");
        var expected = 1;
        assertEquals(expected, res);
    }

    @Test
    void AssignmentBool() {
        setGlobalVar(true);

        var res = (Boolean) eval("VERSION=true");
        assertTrue(res);
    }

    @Test
    void AssignmentBoolDifferent() {
        setGlobalVar(true);

        var res = (Boolean) eval("VERSION=false");
        assertFalse(res);
    }

    @Test
    void AssignmentDecimalSame() {
        setGlobalVar(1.1);

        var res = eval("VERSION=1.1");
        var expected = 1.1;
        assertEquals(expected, res);
    }

    @Test
    void AssignmentDecimalDifferent() {
        setGlobalVar(1.1);

        var res = eval("VERSION=1.2");
        var expected = 1.2;
        assertEquals(expected, res);
    }

    @Test
    void AssignAddition() {
        var res = eval("""
                var x=0
                x = 1.1+2.2
                """);
        var expected = 1.1 + 2.2;
        assertEquals(expected, res);
    }

    @Test
    void stringConcat() {
        var res = eval("""
                "hello " + "world"
                """);
        assertEquals("hello world", res);
    }

    @Test
    void stringConcatEmptySpace() {
        var res = eval("""
                "hello" +" "+ "world"
                """);
        assertEquals("hello world", res);
    }

    @Test
    void stringConcatEmptySpaceFront() {
        var res = eval("""
                "  " + "hello " + "world"
                """);
        assertEquals("  hello world", res);
    }

    @Test
    void stringConcatTabFront() {
        var res = eval("""
                "\t" + "hello " + "world"
                """);
        assertEquals("\thello world", res);
    }

    @Test
    void stringConcatNewLineFront() {
        var res = eval("""
                "\n" + "hello " + "world"
                """);
        assertEquals("\nhello world", res);
    }

    @Test
    void stringConcatNumber() {
        var res = eval("""
                2 + " hello" +" "+ 2
                """);
        assertEquals("2 hello 2", res);
    }

    @Test
    void stringConcatNumberStrings() {
        var res = eval("""
                2+2 + " hello" +" "+ 2+2
                """);
        assertEquals("4 hello 22", res);
    }

    @Test
    void stringConcatNumberAddition() {
        var res = eval("""
                (2+2) + " hello" +" "+ (2+2)
                """);
        assertEquals("4 hello 4", res);
    }

    @Test
    void stringConcatDecimal() {
        var res = eval("""
                2.1 + " hello" +" "+ 2.1
                """);
        assertEquals("2.1 hello 2.1", res);
    }

    @Test
    void stringConcatDecimalStrings() {
        var res = eval("""
                2.1+2.1+ " hello" +" "+ 2.1+2.1
                """);
        assertEquals("4.2 hello 2.12.1", res);
    }

    @Test
    void stringConcatDecimalAddition() {
        var res = eval("""
                (2.1+2.1) + " hello" +" "+ (2.1+2.1)
                """);
        assertEquals("4.2 hello 4.2", res);
    }

    @Test
    void AssignMultiplication() {
        var res = eval("""
                var x=0
                x = 1.1*2.2
                """);
        var expected = 1.1 * 2.2;
        assertEquals(expected, res);
    }

    @Test
    void AssignDivision() {
        var res = eval("""
                var x=0
                x = 2.1/2.2
                """);
        var expected = 2.1 / 2.2;
        assertEquals(expected, res);
    }

    @Test
    void AssignBooleanFalse() {
        var res = (Boolean) eval("""
                var x=0
                x = 1==2
                """);
        assertFalse(res);
    }

    @Test
    void AssignBooleanTrue() {
        var res = (Boolean) eval("""
                var x=0
                x = 1==1
                """);
        assertTrue(res);
    }

    @Test
    void AssignLess() {
        var res = (Boolean) eval("""
                var x=0
                x = 3 < 2
                """);
        assertFalse(res);
    }

    @Test
    void AssignLessTrue() {
        var res = (Boolean) eval("""
                var x=0
                x = 3 < 3.1
                """);
        assertTrue(res);
    }

    @Test
    void AssignLessFalse() {
        var res = (Boolean) eval("""
                var x=0
                x = 3.2 < 3.1
                """);
        assertFalse(res);
    }

    @Test
    void AssignGreater() {
        var res = (Boolean) eval("""
                var x=0
                x = 3 > 2
                """);
        assertTrue(res);
    }

    @Test
    void AssignGreaterEq() {
        var res = (Boolean) eval("""
                var x=0
                x = 3 >= 2
                """);
        assertTrue(res);
    }

    @Test
    void AssignGreaterEqTrue() {
        var res = (Boolean) eval("""
                var x=0
                x = 2 >= 2
                """);
        assertTrue(res);
    }

    @Test
    void AssignGreaterEqFalse() {
        var res = (Boolean) eval("""
                var x=0
                x = 1 >= 2
                """);
        assertFalse(res);
    }

    @Test
    void AssignLessEq() {
        var res = (Boolean) eval("""
                var x=0
                x = 3 <= 2
                """);
        assertFalse(res);
    }

    @Test
    void AssignLessEqTrue() {
        var res = (Boolean) eval("""
                var x=0
                x = 2 <= 2
                """);
        assertTrue(res);
    }

    @Test
    void AssignLessPlusAssignment() {
        var res = (Number) eval("""
                var x=1
                x += 2
                """);
        assertEquals(3, res);
    }

    @Test
    void AssignLessEqFalse() {
        var res = (Boolean) eval("""
                var x=0
                x = 1 <= 2
                """);
        assertTrue(res);
        log.warn(res);
    }


}
