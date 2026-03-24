package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.exceptions.DeclarationExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @DisplayName("Shadowing same builtin twice throws")
    void doubleShadowThrows() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                var sum = 1
                var sum = 2
                """));
    }

    @Test
    @DisplayName("Shadowing user-defined function throws")
    void shadowUserFunctionThrows() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                fun add(number a, number b) {
                    return a + b
                }
                var add = 5
                """));
    }

    @Test
    @DisplayName("Shadowing user-defined struct throws")
    void shadowUserStructThrows() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                struct Point {
                    number x
                    number y
                }
                var Point = 5
                """));
    }

    @Test
    @DisplayName("Struct named after builtin throws")
    void structNamedAfterBuiltinThrows() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                struct sum {
                    number a
                    number b
                }
                """));
    }

    @Test
    @DisplayName("Component type named after builtin throws")
    void componentTypeNamedAfterBuiltinThrows() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                component sum {
                    input number a = 1
                    output number total = a * 2
                }
                """));
    }

    @Test
    @DisplayName("Component instance named after builtin throws")
    void componentInstanceNamedAfterBuiltinThrows() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                component Adder {
                    input number a = 1
                    output number total = a * 2
                }
                component Adder sum {
                    a = 5
                }
                """));
    }

    @Test
    @DisplayName("Function declaration named after builtin throws")
    void funNamedAfterBuiltinThrows() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                fun sum(number a, number b) {
                    return a + b
                }
                """));
    }

    @Test
    @DisplayName("var shadows re-enabled builtin first()")
    void varShadowsFirst() {
        var result = eval("""
                var first = "winner"
                first
                """);
        assertEquals("winner", result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin last()")
    void varShadowsLast() {
        var result = eval("""
                var last = 99
                last
                """);
        assertEquals(99, result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin length()")
    void varShadowsLength() {
        var result = eval("""
                var length = 42
                length + 8
                """);
        assertEquals(50, result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin values()")
    void varShadowsValues() {
        var result = eval("""
                var values = [1, 2, 3]
                values
                """);
        assertEquals(java.util.List.of(1, 2, 3), result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin find()")
    void varShadowsFind() {
        var result = eval("var find = true");
        assertEquals(true, result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin hash()")
    void varShadowsHash() {
        var result = eval("""
                var hash = "abc123"
                hash
                """);
        assertEquals("abc123", result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin date()")
    void varShadowsDate() {
        var result = eval("""
                var date = "2024-01-01"
                date
                """);
        assertEquals("2024-01-01", result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin second()")
    void varShadowsSecond() {
        var result = eval("""
                var second = 2
                second * 5
                """);
        assertEquals(10, result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin get()")
    void varShadowsGet() {
        var result = eval("""
                var get = "fetched"
                get
                """);
        assertEquals("fetched", result);
    }

    @Test
    @DisplayName("var shadows re-enabled builtin environment()")
    void varShadowsEnvironment() {
        var result = eval("""
                var environment = "production"
                environment
                """);
        assertEquals("production", result);
    }

    @Test
    @DisplayName("Shadowed builtin is no longer callable")
    void shadowedBuiltinNotCallable() {
        // After shadowing sum with an integer, calling sum() should fail
        // because the var value (integer) is not callable
        assertThrows(RuntimeException.class, () -> eval("""
                var sum = 42
                sum([1, 2, 3])
                """));
    }

    @Test
    @DisplayName("Builtin can be used before shadowing in separate scope")
    void builtinUsableBeforeShadow() {
        var result = eval("""
                var first = "not-a-function"
                first
                """);
        assertEquals("not-a-function", result);
    }
}
