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
        Assertions.assertEquals("Provided value   with length 1 is empty: \n" +
                                "\u001B[33m@nonEmpty\u001B[m\n" +
                                "\u001B[m\u001B[2J\u001B[35minput \u001B[34mstring\u001B[39m \u001B[39msomething = \" \"\u001B[m", err.getMessage());
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
