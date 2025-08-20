package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.ObjectType;
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
                type alias = 1 | 2
                """);
        var tb = checker.getEnv().lookup("alias");
        assertEquals(new UnionType("alias", checker.getEnv(), ValueType.Number), tb);
    }


    @Test
    void allowAssigningANumberToAUnionType() {
        var res = eval("""
                type alias = 1 | 2
                var alias x = 3; // ok since we assign a number. Actual value validation is checked in interpreter
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), ValueType.Number), res);
    }

    @Test
    void allowAssigningANumberToAUnionTypeMixedTypes() {
        var res = eval("""
                type alias = 1 | 2 | "hello" | true | null
                var alias x = 3
                var alias y = "hey"
                var alias z = true
                var alias d = null
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), ValueType.Number, ValueType.String, ValueType.Null, ValueType.Boolean), res);
    }

    @Test
    void unionTypeAlias() {
        var res = eval("""
                type alias = number
                var alias x = 3; // ok since we assign a number. Actual value validation is checked in interpreter
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), ValueType.Number), res);
    }

    @Test
    @DisplayName("type alias of string assign a string")
    void unionTypeAliasString() {
        var res = eval("""
                type alias = string
                var alias x = 'hello'; // ok since we assign a string
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), ValueType.String), res);
    }

    @Test
    @DisplayName("type alias of object assign a object")
    void unionTypeAliasObject() {
        var res = eval("""
                type alias = {}
                var alias x = {env: 2} // ok since we assign a object
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), new ObjectType(checker.getEnv())), res);
    }

    @Test
    @DisplayName("type alias of string assign a boolean string")
    void unionTypeAliasStringBooleanString() {
        var res = eval("""
                type alias = string
                var alias x = 'true'; // ok since we assign a string
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), ValueType.String), res);
    }

    @Test
    @DisplayName("Should throw because we assign the wrong value type")
    void unionTypeNumberBooleanThrows() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                type alias = number
                var alias x = false
                """)
        );
    }

    @Test
    @DisplayName("Should throw because we assign the wrong value type")
    void unionTypeStringBooleanThrows() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                type alias = string
                var alias x = false
                """)
        );
    }


}
