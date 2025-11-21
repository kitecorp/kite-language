package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.Runtime.exceptions.NotFoundException;
import io.kite.TypeChecker.Types.UnionType;
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
        assertEquals("Expected type `null | number | string` with valid values: `null | number | string` but got `array` in expression: `alias x = []`", err.getMessage());
    }


    /* ****************
     * DEDUPLICATION *
     * ****************
     **/

    @Test
    @DisplayName("Union deduplicates duplicate literal number")
    void typeRepeatingInt() {
        eval("type custom = 1 | 1");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate 1 | 1 to just 1");
    }

    @Test
    @DisplayName("Union deduplicates duplicate decimal")
    void typeRepeatingDecimal() {
        eval("type custom = 1.2 | 1.2");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate 1.2 | 1.2 to just 1.2");
    }

    @Test
    @DisplayName("Union deduplicates duplicate string (different quotes)")
    void typeRepeatingString() {
        eval("type custom = 'hi' | \"hi\"");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate 'hi' | \"hi\" to just one string");
    }

    @Test
    @DisplayName("Union deduplicates duplicate boolean")
    void typeRepeatingBoolean() {
        eval("type custom = true | true");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate true | true to just true");
    }

    @Test
    @DisplayName("Union deduplicates duplicate null")
    void typeRepeatingNull() {
        eval("type custom = null | null");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate null | null to just null");
    }

    @Test
    @DisplayName("Union deduplicates duplicate number type")
    void typeRepeatingNumber() {
        eval("type custom = number | number");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate number | number to just number");
    }

    @Test
    @DisplayName("Union deduplicates duplicate string type")
    void typeRepeatingStringKeyword() {
        eval("type custom = string | string");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate string | string to just string");
    }

    @Test
    @DisplayName("Union deduplicates duplicate boolean type")
    void typeRepeatingBooleanKeyword() {
        eval("type custom = boolean | boolean");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate boolean | boolean to just boolean");
    }

    @Test
    @DisplayName("Union deduplicates duplicate object type")
    void typeRepeatingObjectKeyword() {
        eval("type custom = object | object");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate object | object to just object");
    }

    @Test
    @DisplayName("Union deduplicates duplicate empty object")
    void typeRepeatingEmptyObject() {
        eval("type custom = {} | {}");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(), "Should deduplicate {} | {} to just one empty object");
    }

    @Test
    @DisplayName("Union deduplicates duplicate complex object")
    void typeRepeatingObject() {
        eval("type custom = { env: 123, color: 'red' } | { env: 123, color: 'red' }");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should deduplicate identical objects with same properties");
    }

    @Test
    @DisplayName("Union deduplicates duplicate array of ints")
    void typeRepeatingArrayInts() {
        eval("type custom = [1,2,3] | [1,2,3]");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should deduplicate [1,2,3] | [1,2,3] to just one array");
    }

    @Test
    @DisplayName("Union deduplicates duplicate array of decimals")
    void typeRepeatingArrayDecimals() {
        eval("type custom = [1.1, 2.2, 3.3] | [1.1, 2.2, 3.3]");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should deduplicate arrays with same decimal values");
    }

    @Test
    @DisplayName("Union deduplicates duplicate array of boolean")
    void typeRepeatingArrayBoolean() {
        eval("type custom = [true] | [true]");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should deduplicate [true] | [true] to just one array");
    }

    @Test
    @DisplayName("Union deduplicates duplicate array of strings")
    void typeRepeatingArrayStrings() {
        eval("type custom = ['hello'] | ['hello']");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should deduplicate ['hello'] | ['hello'] to just one array");
    }

    @Test
    @DisplayName("Union deduplicates duplicate array of empty objects")
    void typeRepeatingArrayObjectEmpty() {
        eval("type custom = [{}] | [{}]");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should deduplicate [{}] | [{}] to just one array of empty objects");
    }

    @Test
    @DisplayName("Union deduplicates [{}] and [object] as same type")
    void typeRepeatingArrayObjectEmptyKeyword() {
        eval("type custom = [{}] | [object]");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should normalize [{}] and [object] to the same type");
    }

    @Test
    @DisplayName("Union deduplicates duplicate array of object keyword")
    void typeRepeatingArrayObjectKeyword() {
        eval("type custom = [object] | [object]");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should deduplicate [object] | [object] to just one array");
    }

    @Test
    @DisplayName("Union keeps distinct types")
    void typeDistinctTypes() {
        eval("type custom = 1 | 2 | 3");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should keep all distinct literal types");
    }

    @Test
    @DisplayName("Union deduplicates mixed with distinct types")
    void typeMixedDuplicatesAndDistinct() {
        eval("type custom = 1 | 1 | 2 | 2 | 3");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should deduplicate to 1 | 2 | 3");
    }

    @Test
    @DisplayName("Union deduplicates string type and keeps literals separate")
    void typeStringTypeVsLiteral() {
        eval("type custom = string | 'hello' | string");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should have string type and 'hello' literal (deduplicating second string)");
    }

    @Test
    @DisplayName("Union with empty objects and object keyword")
    void typeEmptyObjectsNormalization() {
        eval("type custom = {} | object | {}");
        var unionType = (UnionType) checker.getEnv().lookup("custom");
        assertNotNull(unionType);
        assertEquals(1, unionType.getTypes().size(),
                "Should normalize all empty objects to one type");
    }

}
