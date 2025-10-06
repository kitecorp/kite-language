package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@unique")
public class UniqueTest extends CheckerTest {

    @Test
    void uniqueInvalidArgs() {
        var error = Assertions.assertThrows(TypeError.class, () -> eval("""
                @unique(10)
                input string something"""));
        Assertions.assertEquals("\u001B[33m@unique\u001B[m must not have any arguments", error.getMessage());
    }

    @Test
    void uniqueString() {
        var error = Assertions.assertThrows(TypeError.class, () -> eval("""
                @unique
                input string something"""));
        Assertions.assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34mstring", error.getMessage());
    }

    @Test
    void uniqueNumber() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @unique
                        input number something""")
        );
        Assertions.assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34mnumber", error.getMessage());
    }

    @Test
    void uniqueBoolean() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @unique
                        input boolean something""")
        );
        Assertions.assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34mboolean", error.getMessage());
    }

    @Test
    void uniqueAny() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @unique
                        input any something""")
        );
        Assertions.assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34many", error.getMessage());
    }

    @Test
    void uniqueObject() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @unique
                        input object something""")
        );
        Assertions.assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34mobject", error.getMessage());
    }

    @Test
    void uniqueStringArray() {
        eval("""
                @unique
                input string[] something""");
    }


    @Test
    void uniqueNumberArray() {
        eval("""
                @unique
                input number[] something""");
    }

    @Test
    void uniqueBooleanArray() {
        eval("""
                @unique
                input boolean[] something""");
    }

    @Test
    void uniqueAnyArray() {
        eval("""
                @unique
                input any[] something""");
    }

    @Test
    void uniqueObjectArray() {
        eval("""
                @unique
                input object[] something""");
    }

}
