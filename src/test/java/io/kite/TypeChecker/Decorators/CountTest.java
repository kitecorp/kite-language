package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import jdk.jfr.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("@count decorator")
public class CountTest extends CheckerTest {
    @Test
    void decoratorCount() {
        var res = eval("""
                schema vm {}
                @count(2)
                resource vm something {}""");

    }

    @Test
    void decoratorCountReference() {
        var res = eval("""
                schema vm { string name; }
                @count(2)
                resource vm something {
                    name = "something-$count"
                }""");
    }

    @Test
    @Description("! we don't actually check vm.something[count]. We just say")
    void decoratorMultipleResources() {
        var res = eval("""
                schema vm { string name; }
                @count(2)
                resource vm something {
                    name = "something $count"
                }
                
                @count(2)
                resource vm main {
                    name = vm.something.name
                }
                """);
    }

    @Test
    @Description("! we don't actually check vm.something[count]. We just say")
    void decoratorMultipleResourcesNumber() {
        var res = eval("""
                schema vm { number name; }
                @count(2)
                resource vm something {
                    name = 10
                }
                
                @count(2)
                resource vm main {
                    name = vm.something.name
                }
                """);
    }

    @Test
    void decoratorCountZero() {
        eval("""
                @count(0)
                component vm {}""");
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
    @DisplayName("decorator @count with decimal value rounds it to the floor")
    void decoratorCountZeroDecimal() {
        eval("""
                @count(0.7)
                component vm {}""");
    }


}
