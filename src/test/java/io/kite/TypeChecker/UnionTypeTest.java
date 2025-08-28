package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.Runtime.exceptions.NotFoundException;
import io.kite.TypeChecker.Types.ArrayType;
import io.kite.TypeChecker.Types.ObjectType;
import io.kite.TypeChecker.Types.UnionType;
import io.kite.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void allowReassigningANumberToAUnionType() {
        var res = eval("""
                type alias = 1 | 2
                var alias x = 2;
                x = 3
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
    @DisplayName("type alias of object assign any object")
    void unionTypeAliasObject() {
        var res = eval("""
                type alias  = { env: number}
                var alias x = { env: 2 } // ok since we assign a object
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), new ObjectType(checker.getEnv())), res);
    }

    @Test
    @DisplayName("type alias of object assign a specific object")
    void unionTypeAliasSpecificObject() {
        var res = eval("""
                type alias  = { env: number}
                var alias x = { env: 2 } // ok since we assign a object
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), new ObjectType(checker.getEnv())), res);
    }

    @Test
    @Disabled("type alias of object assign a specific object")
    void unionTypeAliasNestedSpecificObject() {
        var res = eval("""
                type someType  = { env: number | 10 } // this should be supported
                var someType x = { env: 'hello' } // ok since we assign a object
                """);
        assertEquals(new UnionType("alias", checker.getEnv(), new ObjectType(checker.getEnv())), res);
    }

    @Test
    @DisplayName("throw because variable was not declared inside the object")
    void throwSinceVariableWasNotDeclaredInsideTheObject() {
        Assertions.assertThrows(NotFoundException.class, () ->
                eval("""
                        type alias = { env: number }
                        var alias x = { count: 2 } // throws because count was not declared in alias object
                        """)
        );
    }

    @Test
    @DisplayName("throw because variable was declared with a different type in the object")
    void throwSinceVariableWasDeclaredWithADifferentTypeInsideTheObject() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        type alias  = { env: number }
                        var alias x = { env: 'hello' } // throws because env is of wrong type in alias object
                        """)
        );
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

    @Test
    @DisplayName("Should throw because we assign the wrong value type")
    void unionTypeNumberArrayThrows() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        type alias = 1 | 2
                        var alias x = [1,2]
                        """)
        );
    }

    @Test
    @DisplayName("Should not throw because we assign the correct array type")
    void unionTypeArrayOfNumbersShouldNotThrowWhenAssigningCorrectArrayType() {
        eval("""
                type alias = 1 | 2
                var alias[] x = [1, 2]
                """);
    }

    @Test
    @DisplayName("Should throw because we assign the incorrect array type")
    void unionTypeArrayOfNumbersThrowsWhenAssigningWrongArrayType() {
        assertThrows(TypeError.class, () -> eval("""
                type alias = 1 | 2
                var alias[] x = ['hello']
                """)
        );
    }

    @Test
    @DisplayName("Should throw because we assign the incorrect array type")
    void unionTypeArrayOfNumbersThrowsWhenAssigningWrongArrayTypeTrue() {
        assertThrows(TypeError.class, () -> eval("""
                type alias = 1 | 2
                var alias[] x = [true]
                """)
        );
    }

    @Test
    @DisplayName("Should throw because we assign the incorrect array type")
    void unionTypeArrayOfNumbersThrowsWhenAssigningWrongArrayTypeFalse() {
        assertThrows(TypeError.class, () -> eval("""
                type alias = 1 | 2
                var alias[] x = [false]
                """)
        );
    }

    @Test
    @DisplayName("Should throw because we assign the incorrect array type")
    void unionTypeArrayOfNumbersThrowsWhenAssigningWrongArrayTypeObject() {
        assertThrows(TypeError.class, () -> eval("""
                type alias = 1 | 2
                var alias[] x = [{env: 'dev'}]
                """)
        );
    }

    @Test
    @DisplayName("Should be fine since we assign a number. The actual value needs to be checked by interpreter")
    void unionTypeArrayOfNumbersThrowsWhenAssigningWrongArrayTypeInt() {
        var res = eval("""
                type alias = 1 | 2
                var alias[] x = [3]
                """);
        assertEquals(new ArrayType(checker.getEnv(), new UnionType("alias", checker.getEnv(), ValueType.Number)), res);
    }


}
