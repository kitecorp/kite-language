package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Log4j2
@DisplayName("TypeChecker String")
class StringTest extends CheckerTest {

    @Test
    void testStringLiteral() {
        var t1 = checker.visit("hello");
        Assertions.assertEquals(t1, ValueType.String);
    }

    @Test
    void testNumberWithStringAddition() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                "1"+1
                """));
    }

    @Test
    void testStringWithNumberAddition() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                1+"1"
                """));
    }

    @Test
    void testStringWithNumberEq() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                1 == "1"
                """));
    }

    @Test
    void testStringWithStringAddition() {
        var actual = eval("""
                "hello" + "world"
                """);
        Assertions.assertEquals(actual, ValueType.String);
    }

    @Test
    void testStringWithStringSubstraction() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                "hello" - "world"
                """));
    }

    @Test
    void testStringWithStringDivision() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                "hello" / "world"
                 """));
    }

    @Test
    void testStringWithStringMultiplication() {
        Assertions.assertThrows(TypeError.class, () -> eval(("""
                "hello" * "world"
                """)));
    }

    @Test
    void testStringWithStringMod() {
        Assertions.assertThrows(TypeError.class, () -> eval(("""
                "hello" % "world"
                """)));
    }

    @Test
    void testStringWithNumberMod() {
        Assertions.assertThrows(TypeError.class, () -> eval(("""
                1 % "1"
                """)));
    }

    @Test
    void testNumberWithStringMod() {
        Assertions.assertThrows(TypeError.class, () -> eval(("""
                1 % "1"
                """)));
    }


}