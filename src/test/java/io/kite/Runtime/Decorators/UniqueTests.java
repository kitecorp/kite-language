package io.kite.Runtime.Decorators;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Log4j2
public class UniqueTests extends DecoratorTests {

    @Test
    void uniqueStringArrayEmpty() {
        eval("""
                @unique
                input string[] something = []
                """);
    }

    @Test
    void uniqueStringArray() {
        eval("""
                @unique
                input string[] something = ["hello"]
                """);
    }

    @Test
    void uniqueStringArrayMultiple() {
        eval("""
                @unique
                input string[] something = ["hello", "world"]
                """);
    }

    @Test
    void uniqueStringArrayError() {
        var err = Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @unique
                input string[] something = ["hello", "hello"]
                """)
        );
        Assertions.assertEquals("""
                Provided list [hello, hello] has duplicate elements:
                [33m@unique[m
                [m[2J[35minput [34mstring[][39m [39msomething = ["hello", "hello"][m""", err.getMessage());
    }

    @Test
    void uniqueNumberArrayEmpty() {
        eval("""
                @unique
                input number[] something = []
                """);
    }

    @Test
    void uniqueNumberArray() {
        eval("""
                @unique
                input number[] something = [10]
                """);
    }

    @Test
    void uniqueNumberArrayMultiple() {
        eval("""
                @unique
                input number[] something = [1,2,3]
                """);
    }

    @Test
    void uniqueNumberArrayError() {
        var err = Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @unique
                input number[] something = [1,1]
                """)
        );
        Assertions.assertEquals("""
                Provided list [1, 1] has duplicate elements:
                [33m@unique[m
                [m[2J[35minput [34mnumber[][39m [39msomething = [1, 1][m""", err.getMessage());
    }


}
