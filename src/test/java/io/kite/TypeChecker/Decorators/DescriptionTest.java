package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@description decorator")
public class DescriptionTest extends CheckerTest {

    @Test
    void decoratorDescriptionResource() {
        var res = eval("""
                schema vm {}
                @description("some markdown")
                resource vm something {}""");

    }

    @Test
    void decoratorDescriptionComponent() {
        var res = eval("""
                @description("some markdown")
                component something {}""");
    }

    @Test
    @DisplayName("decorator @description only allows strings as arguments")
    void decoratorDescriptionError() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @description(10)
                component something {}""")
        );
    }

    @Test
    void decoratorDescriptionVar() {
        var res = eval("""
                @description("some markdown")
                var something = 2""");
    }

    @Test
    void decoratorDescriptionSchema() {
        var res = eval("""
                @description("some markdown")
                schema something{}""");
    }

}
