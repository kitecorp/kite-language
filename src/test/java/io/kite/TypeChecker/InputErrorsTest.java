package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Input")
public class InputErrorsTest extends CheckerTest {
    /// STRING
    @Test
    void inputStringInitError() {
        assertThrows(TypeError.class, () -> eval("input string something = 10"));
    }

    @Test
    void inputStringInitDecimalError() {
        assertThrows(TypeError.class, () -> eval("input string something = 0.1"));
    }

    @Test
    void inputStringInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("input string something = true"));
    }

    @Test
    void inputStringInitNullError() {
        assertThrows(TypeError.class, () -> eval("input string something = null"));
    }

    @Test
    void inputStringInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("input string something = {}"));
    }

    @Test
    void inputStringInitObjectEmptyKeywordError() {
        assertThrows(TypeError.class, () -> eval("input string something = object"));
    }

    @Test
    void inputStringInitObjectEmptyKeywordNoBodyError() {
        assertThrows(TypeError.class, () -> eval("input string something = object()"));
    }

    @Test
    void inputStringInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("input string something = object({})"));
    }

    @Test
    void inputStringInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input string something = object({ env: 'dev'})"));
    }

    @Test
    void inputStringInitObjectError() {
        assertThrows(TypeError.class, () -> eval("input string something = { env: 'dev' }"));
    }

    @Test
    void inputStringInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("input string something = ['hello']"));
    }

    @Test
    void inputStringInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("input string something = [1,2,3]"));
    }

    @Test
    void inputStringInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("input string something = [true,false]"));
    }

    @Test
    void inputStringInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("input string something = [true,false]"));
    }

    @Test
    void inputStringInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("input string something = [{}, {}]"));
    }

    @Test
    void inputStringInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("input string something = [object(), object()]"));
    }

    @Test
    void inputStringInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("input string something = [object({}), object({})]"));
    }

    @Test
    void inputStringInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input string something = [object, object]"));
    }

    @Test
    void inputStringInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input string something = [{env: 'dev'}, {env: 'dev'}]"));
    }


    /// NUMBER

    @Test
    void inputNumberInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("input number something = true"));
    }

    @Test
    void inputNumberInitNullError() {
        assertThrows(TypeError.class, () -> eval("input number something = null"));
    }

    @Test
    void inputNumberInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("input number something = {}"));
    }

    @Test
    void inputNumberInitObjectEmptyKeywordError() {
        assertThrows(TypeError.class, () -> eval("input number something = object"));
    }

    @Test
    void inputNumberInitObjectEmptyKeywordNoBodyError() {
        assertThrows(TypeError.class, () -> eval("input number something = object()"));
    }

    @Test
    void inputNumberInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("input number something = object({})"));
    }

    @Test
    void inputNumberInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input number something = object({ env: 'dev'})"));
    }

    @Test
    void inputNumberInitObjectError() {
        assertThrows(TypeError.class, () -> eval("input number something = { env: 'dev' }"));
    }

    /// BOOLEAN
    @Test
    void inputBooleanInitError() {
        assertThrows(TypeError.class, () -> eval("input boolean something = 123"));
    }

    @Test
    void inputObjectInitEmptyError() {
        assertThrows(TypeError.class, () -> eval("input object something = true"));
    }

    @Test
    void inputObjectInitError() {
        assertThrows(TypeError.class, () -> eval("input object something = 'hello'"));
    }

    @Test
    void inputUnionInitError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom something = true
                """));
    }

    @Test
    void inputStringArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input string[] something=[1,2,3]"));
    }

    @Test
    void inputNumberArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input number[] something=['hi']"));
    }

    @Test
    void inputBooleanArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something=[1]"));
    }

    @Test
    void inputObjectArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input object[] something=[1]"));
    }


    @Test
    void inputUnionArrayInitError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = [true]
                """));
    }

}
