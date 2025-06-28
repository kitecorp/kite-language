package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker Loops")
public class LoopTest extends CheckerTest {

    @Test
    void testBlock() {
        var actual = eval("""
                var x = 10
                while (x!=0) {
                   x--
                }
                x
                """);
        assertEquals(ValueType.Number, actual);
    }


    @Test
    void increment() {
        var res = eval("""
                 var a = 0
                 var number temp = 0
                 for (var b = 1; a < 100; b = temp + b) {
                   temp = a;
                   a = b;
                 }
                """);
        assertEquals(ValueType.Number, res);
    }

    @Test
    void incrementEq() {
        var res = eval("""
                 for (var a = 1; a < 10; a=a+1) {
                   a;
                 }
                """);
        assertEquals(ValueType.Number, res);
    }

    @Test
    void incrementTestOnly() {
        var res = eval("""
                var a = 1
                 for (; a < 10; a=a+1) {
                   a
                 }
                """);
        assertEquals(ValueType.Number, res);
    }

    @Test
    void stringConcatenation() {
        var res = eval("""
                var test = ""
                for (var a = 1; a < 10; a=a+1) {
                   test+=toString(a);
                 }
                 test
                """);
        assertEquals(ValueType.String, res);
    }


}
