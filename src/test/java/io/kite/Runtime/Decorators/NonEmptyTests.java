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
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @nonEmpty
                input string something = " "
                """));
    }

    @Test
    void nonEmptyArrayEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @nonEmpty
                input string[] something = []
                """));
    }

    @Test
    void nonEmptyArray() {
        eval("""
                @nonEmpty
                input string[] something = ["hello"]
                """);
    }

    @Test
    void nonEmptyArrayNumber() {
        eval("""
                @nonEmpty
                input number[] something = [10]
                """);
    }


}
