package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.AnyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Annotation")
public class DecoratorTest extends CheckerTest {

    @Test
    void decoratorSensitive() {
        var res = eval("""
                @sensitive
                output any something = null""");

        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void decoratorSensitiveInvalidArgs() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @sensitive(2)
                output any something = null
                """));
    }

    @Test
    void decoratorSensitiveInvalidElement() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @sensitive
                var x = 2"""));
    }

    @Test
    void decoratorUnkown() {
        assertThrows(TypeError.class, () -> eval("""
                @sensitiveUnknown
                output any something = null"""));

    }

    @Test
    void decoratorCountError() {
        assertThrows(TypeError.class, () -> eval("""
                @count(2)
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorBoolean() {
        assertThrows(TypeError.class, () -> eval("""
                @count(true)
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorString() {
        assertThrows(TypeError.class, () -> eval("""
                @count("2")
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorArrayString() {
        assertThrows(TypeError.class, () -> eval("""
                @count(["2"])
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorArrayNumber() {
        assertThrows(TypeError.class, () -> eval("""
                @count([2])
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorObject() {
        assertThrows(TypeError.class, () -> eval("""
                @count({env: 2})
                output any something = null"""));

    }

    @Test
    void decoratorCount() {
        var res = eval("""
                schema vm {}
                @count(2)
                resource vm something {}""");

    }

    @Test
    void decoratorCountMax() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @count(1000)
                component vm {}""")
        );
    }

    @Test
    void decoratorCountMin() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @count(-10)
                component vm {}""")
        );
    }

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
    void decoratorDescriptionVar() {
        var res = eval("""
                @description("some markdown")
                var something = 2""");
    }

    @Test
    void decoratorDescriptionFun() {
        var res = eval("""
                @description("some markdown")
                fun something(){return void;}""");
    }

    @Test
    void decoratorDescriptionSchema() {
        var res = eval("""
                @description("some markdown")
                schema something{}""");
    }

}
