package io.kite.Runtime.Decorators;

import io.kite.TypeChecker.TypeError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Log4j2
public class MinMaxLengthTests extends DecoratorTests {

    @Test
    void outputMaxLength() {
        eval("""
                @maxLength(10)
                output string something = "hello"
                """);
    }

    @Test
    void outputMaxLengthGreaterThan() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @maxLength(10)
                output string something = "hello world!!!!!!!!!!!"
                """));
    }

    @Test
    void outputMaxLengthWrongInit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxLength(10)
                output string something = 10
                """));
    }

    @Test
    void outputMaxLengthEmpty() {
        eval("""
                @maxLength(0)
                output string something = ""
                """);
    }

    @Test
    void outputMaxLengthNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @maxLength(0)
                output string something = "a"
                """));
    }

    @Test
    void outputMinLength() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @minLength(10)
                output string something = "hello"
                """));
    }

    @Test
    void outputMinLengthGreaterThan() {
        eval("""
                @minLength(10)
                output string something = "hello world!!!!!!!!!!!"
                """);
    }

    @Test
    void outputMinLengthWrongInit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minLength(10)
                output string something = 10
                """));
    }

    @Test
    void outputMinLengthEmpty() {
        eval("""
                @minLength(0)
                output string something = ""
                """);
    }

    @Test
    void outputMinLengthNegative() {
        eval("""
                @minLength(0)
                output string something = "a"
                """);
    }

    @Test
    void outputMinLengthArray() {
        eval("""
                @minLength(0)
                output string[] something = []
                """);
    }

    @Test
    void outputMinLengthArrayThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @minLength(2)
                output string[] something = ["hi"]
                """)
        );
    }

    @Test
    void outputMinLengthArrayNumber() {
        eval("""
                @minLength(0)
                output number[] something = []
                """);
    }

    @Test
    void outputMinLengthArrayNumberThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @minLength(2)
                output number[] something = [10]
                """)
        );
    }

}
