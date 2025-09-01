package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.Runtime.exceptions.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

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



}
