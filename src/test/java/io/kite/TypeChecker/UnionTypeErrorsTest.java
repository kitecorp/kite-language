package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.Runtime.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeChecker Var")
public class UnionTypeErrorsTest extends CheckerTest {


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
    @DisplayName("throw because variable was not declared inside the object")
    void throwSinceVariableWasNotDeclaredInsideTheObjectKeyword() {
        Assertions.assertThrows(NotFoundException.class, () ->
                eval("""
                        type alias = object({ env: number })
                        var alias x = { count: 2 } // throws because count was not declared in alias object
                        """)
        );
    }

    @Test
    @DisplayName("throw because variable was declared with a different type in the object")
    void throwSinceVariableWasDeclaredWithADifferentTypeInsideTheObjectKeyword() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        type alias  = object({ env: number })
                        var alias x = { env: 'hello' } // throws because env is of wrong type in alias object
                        """)
        );
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
    @DisplayName("Should throw because we init array to a non array type")
    void unionTypeAliasNumberAndStringAllowEmptyInit() {
        var err = assertThrows(TypeError.class, () -> eval("""
                type alias = number | string | null
                var alias x = []
                """));
        assertEquals("Expected type `string | number | null` with valid values: `string | number | null` but got `array` in expression: `alias x = []`", err.getMessage());
    }


    @Test
    @DisplayName("Should throw because union has duplicate literal number")
    void typeRepeatingIntError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = 1 | 1"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("1"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate decimal")
    void typeRepeatingDecimalError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = 1.2 | 1.2"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("1.2"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate string (different quotes)")
    void typeRepeatingStringError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = 'hi' | \"hi\""));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("hi"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate boolean")
    void typeRepeatingBooleanError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = true | true"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("true"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate null")
    void typeRepeatingNullError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = null | null"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate number type")
    void typeRepeatingNumberError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = number | number"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("number"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate string type")
    void typeRepeatingStringKeywordError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = string | string"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("string"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate boolean type")
    void typeRepeatingBooleanKeywordError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = boolean | boolean"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("boolean"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate object type")
    void typeRepeatingObjectKeywordError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = object | object"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("object"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate empty object")
    void typeRepeatingEmptyObjectError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = {} | {}"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("object"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate complex object")
    void typeRepeatingObjectError() {
        var err = assertThrows(TypeError.class, () ->
                eval("type custom = { env: 123, color: 'red' } | { env: 123, color: 'red' }"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("object"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate array of ints")
    void typeRepeatingArrayIntsError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = [1,2,3] | [1,2,3]"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("array"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate array of decimals")
    void typeRepeatingArrayDecimalsError() {
        var err = assertThrows(TypeError.class, () ->
                eval("type custom = [1.1, 2.2, 3.3] | [1.1, 2.2, 3.3]"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("array"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate array of boolean")
    void typeRepeatingArrayBooleanError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = [true] | [true]"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("array"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate array of strings")
    void typeRepeatingArrayStringsError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = ['hello'] | ['hello']"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("array"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate array of empty objects")
    void typeRepeatingArrayObjectEmptyError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = [{}] | [{}]"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("array"));
    }

    @Test
    @DisplayName("Should throw because [{}] and [object] are both arrays of empty objects")
    void typeRepeatingArrayObjectEmptyKeywordError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = [{}] | [object]"));
        assertTrue(err.getMessage().contains("duplicate") ||
                   err.getMessage().contains("array") ||
                   err.getMessage().contains("object"));
    }

    @Test
    @DisplayName("Should throw because union has duplicate array of object keyword")
    void typeRepeatingArrayObjectKeywordError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = [object] | [object]"));
        assertTrue(err.getMessage().contains("duplicate") || err.getMessage().contains("array"));
    }


}
