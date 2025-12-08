package cloud.kitelang.semantics.decorators;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@nonEmpty")
public class NonEmptyTest extends CheckerTest {

    @Test
    void nonEmpty() {
        eval("""
                @nonEmpty
                input string something""");
    }

    @Test
    void nonEmptyArray() {
        eval("""
                @nonEmpty
                input string[] something""");
    }


    @Test
    void nonEmptyArrayNumber() {
        eval("""
                @nonEmpty
                input number[] something""");
    }

    @Test
    void nonEmptyMissingNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @nonEmpty
                input number something"""));
    }

    @Test
    void nonEmptyNumber() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @nonEmpty(10)
                        input number something""")
        );
    }



    @Test
    void nonEmptyArrayAny() {
        eval("""
                @nonEmpty
                input any[] something""");
    }

    @Test
    void nonEmptyArrayObject() {
        eval("""
                @nonEmpty
                input object[] something""");
    }

    @Test
    void nonEmptySchema() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @nonEmpty
                schema something{}"""));
    }

    @Test
    void nonEmptyNegative() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @nonEmpty(-10)
                input string something"""));
    }

}
