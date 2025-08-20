package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.UnionType;
import io.kite.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker Var")
public class UnionTypeTest extends CheckerTest {


    @Test
    void unionNumber() {
        eval("""
                type num = 1 | 2
                """);
        var tb = checker.getEnv().lookup("num");
        assertEquals(new UnionType("num", checker.getEnv(), Set.of(ValueType.Number)), tb);
    }


    @Test
    void allowAssigningANumberToAUnionType() {
        var res = eval("""
                type num = 1 | 2
                var num x = 3; // ok since we assign a number. Actual value validation is checked in interpreter
                """);
        assertEquals(new UnionType("num", checker.getEnv(), Set.of(ValueType.Number)), res);
    }

    @Test
    void allowAssigningANumberToAUnionTypeMixedTypes() {
        var res = eval("""
                type num = 1 | 2 | "hello" | true | null
                var num x = 3
                var num y = "hey"
                var num z = true
                var num d = null
                """);
        assertEquals(new UnionType("num", checker.getEnv(), Set.of(ValueType.Number, ValueType.String, ValueType.Null, ValueType.Boolean)), res);
    }

    @Test
    void unionTypeAlias() {
        var res = eval("""
                type num = number
                var num x = 3; // ok since we assign a number. Actual value validation is checked in interpreter
                """);
        assertEquals(new UnionType("num", checker.getEnv(), Set.of(ValueType.Number)), res);
    }

    @Test
    @DisplayName("Should throw because we assign the wrong value type")
    void unionTypeAliasThrows() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                type num = number
                var num x = false
                """)
        );
    }

    @Test
    void testImplicitNotMatchExplicit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var number a = true;
                """));
    }

    @Test
    @Disabled
    void testInferTypeFromValVar() {
        eval("""
                val a = 42;
                var b = a;
                """);
        var ta = checker.getEnv().lookup("a");
        var tb = checker.getEnv().lookup("b");
        assertEquals(ValueType.Number, ta);
        assertEquals(ValueType.Number, tb);
    }

    @Test
    @Disabled
    void testInferTypeFromVarVal() {
        eval("""
                var a = 42;
                val b = a;
                """);
        var ta = checker.getEnv().lookup("a");
        var tb = checker.getEnv().lookup("b");
        assertEquals(ValueType.Number, ta);
        assertEquals(ValueType.Number, tb);
    }


    @Test
    void testInvalidReAssignmentObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var x = null
                x = false
                """));
    }

    @Test
    void testInvalidReAssignmentNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var x = true
                x = 1
                """));
    }

    @Test
    void testInvalidReAssignmentString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var x = true
                x = "sun"
                """));
    }

    @Test
    void testInvalidReAssignment() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var x = true
                x = null
                """));
    }
}
