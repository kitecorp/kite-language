package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.exceptions.DeclarationExistsException;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that user-declared variables can shadow stdlib builtin function names,
 * while still preventing duplicate user declarations.
 */
@DisplayName("Builtin Function Shadowing")
public class BuiltinShadowingTest extends RuntimeTest {

    @Test
    @DisplayName("var can shadow builtin sum()")
    void varShadowsSum() {
        var result = eval("var sum = 1 + 2");
        assertEquals(3, result);
    }

    @Test
    @DisplayName("var can shadow builtin min()")
    void varShadowsMin() {
        var result = eval("var min = 42");
        assertEquals(42, result);
    }

    @Test
    @DisplayName("var can shadow builtin max()")
    void varShadowsMax() {
        var result = eval("var max = 100");
        assertEquals(100, result);
    }

    @Test
    @DisplayName("Shadowed variable holds correct value")
    void shadowedVariableUsable() {
        var result = eval("""
                var sum = 10 + 20
                sum * 2
                """);
        assertEquals(60, result);
    }

    @Test
    @DisplayName("Duplicate user var still throws")
    void duplicateUserVarThrows() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                var x = 1
                var x = 2
                """));
    }

    @Test
    @DisplayName("Struct input expression with shadowed builtin name")
    void structInputWithShadowedBuiltin() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                input Point origin = Point(10, 20)
                var sum = origin.x + origin.y
                sum
                """);
        assertEquals(30, result);
    }

    @Test
    @DisplayName("Struct field with builtin name")
    void structFieldWithBuiltinName() {
        var result = eval("""
                struct Stats {
                    number sum
                    number min
                    number max
                }
                var s = Stats(100, 0, 200)
                s.sum
                """);
        assertEquals(100, result);
    }

    @Test
    @DisplayName("Struct instantiation assigned to var with builtin name")
    void structInstantiationToBuiltinNameVar() {
        var result = eval("""
                struct Pair {
                    number x
                    number y
                }
                var length = Pair(3, 4)
                length.x + length.y
                """);
        assertEquals(7, result);
    }

    @Test
    @DisplayName("Component output uses var that shadows builtin")
    void componentOutputUsesVarShadowingBuiltin() {
        eval("""
                component Adder {
                    input number a = 1
                    input number b = 2
                    output number total = a + b
                }
                component Adder myAdder {
                    a = 10
                    b = 20
                }
                var sum = myAdder.total
                """);

        assertEquals(30, interpreter.getVar("sum"));
    }

    @Test
    @DisplayName("Multiple vars shadow different builtins in same scope")
    void multipleVarsShadowBuiltins() {
        var result = eval("""
                var sum = 10
                var min = 1
                var max = 100
                sum + min + max
                """);
        assertEquals(111, result);
    }

    @Test
    @DisplayName("Struct named after builtin can be declared and instantiated")
    void structNamedAfterBuiltin() {
        var result = eval("""
                struct sum {
                    number a
                    number b
                }
                var s = sum(3, 7)
                s.a + s.b
                """);
        assertEquals(10, result);
    }

    @Test
    @DisplayName("Component type named after builtin")
    void componentTypeNamedAfterBuiltin() {
        eval("""
                component sum {
                    input number a = 1
                    input number b = 2
                    output number total = a + b
                }
                component sum mySum {
                    a = 10
                    b = 20
                }
                """);

        var instance = interpreter.getComponent("mySum");
        assertEquals(30, instance.argVal("total"));
    }

    @Test
    @DisplayName("Component instance named after builtin")
    void componentInstanceNamedAfterBuiltin() {
        eval("""
                component Adder {
                    input number a = 1
                    input number b = 2
                    output number total = a + b
                }
                component Adder sum {
                    a = 5
                    b = 15
                }
                """);

        var instance = interpreter.getComponent("sum");
        assertEquals(20, instance.argVal("total"));
    }
}
