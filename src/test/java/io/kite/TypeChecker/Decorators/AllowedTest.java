package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@allowed decorator")
public class AllowedTest extends CheckerTest {

    @Test
    void decoratorAllowMissingNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed
                input string something"""));
    }

    @Test
    void decoratorAllow() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input string something"""));
    }

    @Test
    void decoratorAllowStrings() {
        eval("""
                @allowed(["hello", "world"])
                input string something""");
    }

    @Test
    void decoratorAllowNumber() {
        eval("""
                @allowed([10, 20])
                input number something""");
    }

    @Test
    void decoratorAllowStringsArray() {
        eval("""
                @allowed(["hello", "world"])
                input string[] something""");
    }

    @Test
    void decoratorAllowNumberArray() {
        eval("""
                @allowed([10, 20])
                input number[] something""");
    }

    @Test
    void decoratorAllowArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input string[] something"""));
    }

    @Test
    void decoratorAllowArrayNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input number[] something"""));
    }

    @Test
    void decoratorAllowArrayAny() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input any[] something"""));
    }

    @Test
    void decoratorAllowArrayObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input object[] something"""));
    }

    @Test
    void decoratorAllowTrueArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(true)
                input object[] something"""));
    }

    @Test
    void decoratorAllowSchema() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed
                schema something{}"""));
    }

    @Test
    void decoratorAllowNegative() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(-10)
                input string something"""));
    }

}
