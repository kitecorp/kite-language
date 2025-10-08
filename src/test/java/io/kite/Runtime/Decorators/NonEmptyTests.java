package io.kite.Runtime.Decorators;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Log4j2
public class NonEmptyTests extends DecoratorTests {

    @Test
    void nonEmpty() {
        eval("""
                @nonEmpty
                input string something = "hello"
                """);
    }

    @Test
    void nonEmptyNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @nonEmpty
                input string something
                """));
    }

    @Test
    void nonEmptyBlank() {
        var err = Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @nonEmpty
                input string something = " "
                """));
        Assertions.assertEquals("""
                Provided value   with length 1 is empty:\s
                [33m@nonEmpty[m
                [m[2J[35minput [34mstring[39m [39msomething = [32m" "[39m[m""".trim(), err.getMessage());
    }

    @Test
    void nonEmptyArrayEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @nonEmpty
                input string[] something = []
                """));
    }

    @Test
    void nonEmptyStringArray() {
        eval("""
                @nonEmpty
                input string[] something = ["hello"]
                """);
    }

    @Test
    void nonEmptyBooleanArray() {
        eval("""
                @nonEmpty
                input boolean[] something = [true]
                """);
    }

    @Test
    void nonEmptyArrayNumber() {
        eval("""
                @nonEmpty
                input number[] something = [10]
                """);
    }

    @Test
    void nonEmptyArrayAnyNumber() {
        eval("""
                @nonEmpty
                input any[] something = [10]
                """);
    }

    @Test
    void nonEmptyArrayAnyString() {
        eval("""
                @nonEmpty
                input any[] something = ["hello"]
                """);
    }

    @Test
    void nonEmptyArrayObject() {
        eval("""
                @nonEmpty
                input object[] something = [{env: 'prod'}]
                """);
    }


}
