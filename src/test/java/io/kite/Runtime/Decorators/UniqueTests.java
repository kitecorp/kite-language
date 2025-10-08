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
                @unique
                input string[] something = ["hello", "hello"]""", err.getMessage());
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
                @unique
                input number[] something = [1, 1]""", err.getMessage());
    }
    @Test
    void uniqueAnyArrayEmpty() {
        eval("""
                @unique
                input any[] something = []
                """);
    }

    @Test
    void uniqueAnyArray() {
        eval("""
                @unique
                input any[] something = [10]
                """);
    }

    @Test
    void uniqueAnyArrayMultiple() {
        eval("""
                @unique
                input any[] something = [1,2,3]
                """);
    }

    @Test
    void uniqueAnyArrayError() {
        var err = Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @unique
                input any[] something = [1,1]
                """)
        );
        System.out.println(err.getMessage());
        Assertions.assertEquals("""
                Provided list [1, 1] has duplicate elements:
                @unique
                input any[] something = [1, 1]""", err.getMessage());
    }
    @Test
    void uniqueObjectArrayEmpty() {
        eval("""
                @unique
                input object[] something = []
                """);
    }

    @Test
    void uniqueObjectArrayEmptyObject() {
        eval("""
                @unique
                input object[] something = [{}]
                """);
    }

    @Test
    void uniqueObjectArray() {
        eval("""
                @unique
                input object[] something = [{env: 'prod'}]
                """);
    }

    @Test
    void uniqueObjectArrayMultiple() {
        eval("""
                @unique
                input object[] something = [{env: 'prod'}, {env: 'dev'}]
                """);
    }

    @Test
    void uniqueObjectArrayError() {
        var err = Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @unique
                input object[] something = [{env: 'prod'}, {env: 'prod'}]
                """)
        );
        System.out.println(err.getMessage());
        Assertions.assertEquals("""
                Provided list [{env=prod}, {env=prod}] has duplicate elements:
                @unique
                input object[] something = [{
                 "env": "prod"\s
                }, {
                 "env": "prod"\s
                }]""", err.getMessage());
    }


}
