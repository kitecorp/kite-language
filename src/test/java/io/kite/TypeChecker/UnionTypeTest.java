package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.UnionType;
import io.kite.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker Var")
public class UnionTypeTest extends CheckerTest {


    @Test
    void unionNumber() {
        eval("""
                type num = 1 | 2
                """);
        var tb = checker.getEnv().lookup("num");
        assertEquals(new UnionType("num", checker.getEnv(), ValueType.Number), tb);
    }


    @Test
    void allowAssigningANumberToAUnionType() {
        var res = eval("""
                type num = 1 | 2
                var num x = 3; // ok since we assign a number. Actual value validation is checked in interpreter
                """);
        assertEquals(new UnionType("num", checker.getEnv(), ValueType.Number), res);
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
        assertEquals(new UnionType("num", checker.getEnv(), ValueType.Number, ValueType.String, ValueType.Null, ValueType.Boolean), res);
    }

    @Test
    void unionTypeAlias() {
        var res = eval("""
                type num = number
                var num x = 3; // ok since we assign a number. Actual value validation is checked in interpreter
                """);
        assertEquals(new UnionType("num", checker.getEnv(), ValueType.Number), res);
    }

    @Test
    void unionTypeAliasString() {
        var res = eval("""
                type num = string
                var num x = 'hello'; // ok since we assign a string
                """);
        assertEquals(new UnionType("num", checker.getEnv(), ValueType.String), res);
    }

    @Test
    @DisplayName("Should throw because we assign the wrong value type")
    void unionTypeNumberBooleanThrows() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                type num = number
                var num x = false
                """)
        );
    }

    @Test
    @DisplayName("Should throw because we assign the wrong value type")
    void unionTypeStringBooleanThrows() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                type num = string
                var num x = false
                """)
        );
    }


}
