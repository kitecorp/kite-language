package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.Frontend.Parser.ParserErrors;
import io.kite.Frontend.Parser.ValidationException;
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
        assertThrows(TypeError.class, () -> eval("""
                type alias = number | string | null
                var alias x = []
                """));
    }


    @Test
    void typeRepeatingIntError() {
        var err = assertThrows(TypeError.class, () -> eval("type custom = 1 | 1"));
        assertEquals("", err.getMessage());
    }

    @Test
    void typeRepeatingDecimalError() {
        parse("type custom = 1.2 | 1.2");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingStringError() {
        parse("type custom = 'hi' | \"hi\" ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingBooleanError() {
        parse("type custom = true | true ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingNullError() {
        parse("type custom = null | null ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingNumberError() {
        parse("type custom = number | number ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }


    @Test
    void typeRepeatingStringKeywordError() {
        parse("type custom = string | string ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingBooleanKeywordError() {
        parse("type custom = boolean | boolean ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingObjectKeywordError() {
        parse("type custom = object | object ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingEmptyObjectError() {
        parse("type custom = {} | {} ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingObjectError() {
        parse("type custom = { env: 123, color: 'red' } | { env: 123, color: 'red' } ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayIntsError() {
        parse("type custom = [1,2,3] | [1,2,3] ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayDecimalsError() {
        parse("type custom = [1.1, 2.2, 3.3] | [1.1, 2.2, 3.3] ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }


    @Test
    void typeRepeatingArrayBooleanError() {
        parse("type custom = [true] | [true] ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayStringsError() {
        parse("type custom = ['hello'] | ['hello'] ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayObjectEmptyError() {
        parse("type custom = [{}] | [{}] ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayObjectEmptyKeywordError() {
        var err = assertThrows(ValidationException.class, () ->
                parse("type custom = [{}] | [object]")
        );
        assertEquals("Missing '=' after: type custom = [{}] | [object]", err.getMessage());
    }

    @Test
    void typeRepeatingArrayObjectKeywordError() {
        parse("type custom = [object] | [object] ");
        assertFalse(ParserErrors.getErrors().isEmpty());
    }


}
