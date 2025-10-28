package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.Frontend.Parser.errors.ErrorList;
import io.kite.TypeChecker.TypeError;
import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.TypeChecker.ComponentTest.assertIsComponentType;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        // valid because we just check if the argument is a number
        eval("""
                @count(1000)
                component vm {}"""
        );
    }

    @Test
    void decoratorCountMaxVar() {
        // valid because we just check if the argument is a number
        eval("""
                var max = 1000
                @count(max)
                component vm {}"""
        );
    }

    @Test
    void decoratorCountMaxVarThrows() {
        // valid because we just check if the argument is a number
        assertThrows(TypeError.class, () -> eval("""
                var max = "1000"
                @count(max)
                component vm {}"""
        ));
    }

    @Test
    void decoratorCountMaxInvalid() {
        // valid because we just check if the argument is a number
        assertThrows(TypeError.class, () -> eval("""
                @count("1000")
                component vm {}"""
        ));
    }

    @Test
    void decoratorCountMin() {
        // valid because we just check if the argument is a number
        eval("""
                @count(-10)
                component vm {}""");
    }

    @Test
    @DisplayName("decorator @count with decimal value rounds it to the floor")
    void decoratorCountZeroDecimal() {
        eval("""
                @count(0.7)
                component vm {}""");
    }

    @Test
    void decoratorCountOnResourceInComponent() {
        var res = eval("""
                schema vm {
                    string name
                }
                
                component app {
                    @count(3)
                    resource vm server {
                        name = "server-$count"
                    }
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertNotNull(appComponent.lookup("server"));
    }

    @Test
    void decoratorCountOnMultipleResourcesInComponent() {
        var res = eval("""
                schema vm {
                    string name
                }
                
                component app {
                    @count(2)
                    resource vm server {
                        name = "server-$count"
                    }
                
                    @count(3)
                    resource vm database {
                        name = "db-$count"
                    }
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertNotNull(appComponent.lookup("server"));
        assertNotNull(appComponent.lookup("database"));
    }

    @Test
    void decoratorCountWithInputInComponent() {
        var res = eval("""
                schema vm {
                    string name
                }
                
                component app {
                    input number instanceCount = 2
                
                    @count(instanceCount)
                    resource vm server {
                        name = "server-$count"
                    }
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertNotNull(appComponent.lookup("server"));
    }

    @Test
    void decoratorCountReferenceFromAnotherCountedResource() {
        var res = eval("""
                schema vm {
                    string id
                    string refId
                }
                
                @count(2)
                resource vm first {
                    id = "first-$count"
                }
                
                @count(2)
                resource vm second {
                    id = "second-$count"
                    refId = vm.first.id
                }
                """);
    }

    @Test
    void decoratorCountOnResourceWithoutCountVariable() {
        // Valid - count without using $count variable
        var res = eval("""
                schema vm {
                    string name
                }
                
                @count(3)
                resource vm server {
                    name = "static-name"
                }
                """);
    }

    @Test
    void decoratorCountMissingArgument() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @count
                resource vm something {}
                """));
    }

    @Test
    void decoratorCountMultipleArguments() {
        assertThrows(ErrorList.class, () -> eval("""
                schema vm {}
                @count(2, 3)
                resource vm something {}
                """));
    }

    @Test
    void decoratorCountOnInput() {
        // @count should not be valid on inputs
        assertThrows(TypeError.class, () -> eval("""
                @count(2)
                input string something
                """));
    }

    @Test
    void decoratorCountOnSchema() {
        // @count should not be valid on schemas
        assertThrows(TypeError.class, () -> eval("""
                @count(2)
                schema vm {}
                """));
    }

    @Test
    void decoratorCountOne() {
        // Edge case: count of 1 (essentially no counting)
        eval("""
                schema vm {
                    string name
                }
                @count(1)
                resource vm server {
                    name = "server"
                }
                """);
    }
}
