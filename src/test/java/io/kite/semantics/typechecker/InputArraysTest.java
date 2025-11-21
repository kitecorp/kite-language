package io.kite.semantics;

import io.kite.base.CheckerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Input")
public class InputArraysTest extends CheckerTest {
    /// STRING
    @Test
    void inputStringInitError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = 10"));
    }

    @Test
    void inputStringInitDecimalError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = 0.1"));
    }

    @Test
    void inputStringInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = true"));
    }

    @Test
    void inputStringInitNullError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = null"));
    }

    @Test
    void inputStringInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = {}"));
    }

    @Test
    void inputStringInitObjectEmptyKeywordNoBodyError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = object()"));
    }

    @Test
    void inputStringInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = object({})"));
    }

    @Test
    void inputStringInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = object({ env: 'dev'})"));
    }

    @Test
    void inputStringInitObjectError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = { env: 'dev' }"));
    }

    @Test
    void inputStringInitArrayStringError() {
        eval("input string[] something = ['hello']");
    }

    @Test
    void inputStringInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = [1,2,3]"));
    }

    @Test
    void inputStringInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = [true,false]"));
    }

    @Test
    void inputStringInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = [null]"));
    }

    @Test
    void inputStringInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = [{}, {}]"));
    }

    @Test
    void inputStringInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = [object(), object()]"));
    }

    @Test
    void inputStringInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = [object({}), object({})]"));
    }

    @Test
    void inputStringInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = [object, object]"));
    }

    @Test
    void inputStringInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input string[] something = [{env: 'dev'}, {env: 'dev'}]"));
    }


    /// NUMBER
    @Test
    void inputNumberInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = true"));
    }

    @Test
    void inputNumberInitNullError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = null"));
    }

    @Test
    void inputNumberInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = {}"));
    }

    @Test
    void inputNumberInitObjectEmptyKeywordNoBodyError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = object()"));
    }

    @Test
    void inputNumberInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = object({})"));
    }

    @Test
    void inputNumberInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = object({ env: 'dev'})"));
    }

    @Test
    void inputNumberInitObjectError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = { env: 'dev' }"));
    }


    @Test
    void inputNumberInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = ['hello']"));
    }

    @Test
    void inputNumberInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = [true,false]"));
    }

    @Test
    void inputNumberInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = [true,false]"));
    }

    @Test
    void inputNumberInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = [{}, {}]"));
    }

    @Test
    void inputNumberInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = [object(), object()]"));
    }

    @Test
    void inputNumberInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = [object({}), object({})]"));
    }

    @Test
    void inputNumberInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = [object, object]"));
    }

    @Test
    void inputNumberInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input number[] something = [{env: 'dev'}, {env: 'dev'}]"));
    }

    /// BOOLEAN

    @Test
    void inputBooleanInitNullError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = null"));
    }

    @Test
    void inputBooleanInitIntError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [1,2,3]"));
    }

    @Test
    void inputBooleanInitDecimalError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [1.2,2.3,3.4]"));
    }

    @Test
    void inputBooleanInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = {}"));
    }

    @Test
    void inputBooleanInitObjectEmptyKeywordNoBodyError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = object()"));
    }

    @Test
    void inputBooleanInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = object({})"));
    }

    @Test
    void inputBooleanInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = object({ env: 'dev'})"));
    }

    @Test
    void inputBooleanInitObjectError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = { env: 'dev' }"));
    }


    @Test
    void inputBooleanInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = ['hello']"));
    }

    @Test
    void inputBooleanInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [1,2,3]"));
    }

    @Test
    void inputBooleanInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [null]"));
    }

    @Test
    void inputBooleanInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [{}, {}]"));
    }

    @Test
    void inputBooleanInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [object(), object()]"));
    }

    @Test
    void inputBooleanInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [object({}), object({})]"));
    }

    @Test
    void inputBooleanInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [object, object]"));
    }

    @Test
    void inputBooleanInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] something = [{env: 'dev'}, {env: 'dev'}]"));
    }

    /// OBJECT
    @Test
    void inputObjectInitNullError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = null"));
    }

    @Test
    void inputObjectInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = true"));
    }

    @Test
    void inputObjectInitNumberError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = 10"));
    }

    @Test
    void inputObjectInitDecimalError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = 0.1"));
    }

    @Test
    void inputObjectInitStringError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = 'hello'"));
    }

    @Test
    void inputObjectInitIntError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = [1,2,3]"));
    }

    @Test
    void inputObjectInitDecimalArrayError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = [1.2,2.3,3.4]"));
    }

    @Test
    void inputObjectInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = ['hello']"));
    }

    @Test
    void inputObjectInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = [1,2,3]"));
    }

    @Test
    void inputObjectInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = [true,false]"));
    }

    @Test
    void inputObjectInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("input object[] something = [null]"));
    }


    /// UNION

    @Test
    void inputUnionInitNullError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = null"""));
    }

    @Test
    void inputUnionInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = true"""));
    }

    @Test
    void inputUnionInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = {}"""));
    }

    @Test
    void inputUnionInitObjectEmptyKeywordNoBodyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = object()"""));
    }

    @Test
    void inputUnionInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = object({})"""));
    }

    @Test
    void inputUnionInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = object({ env: 'dev'})"""));
    }

    @Test
    void inputUnionInitObjectError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = { env: 'dev' }"""));
    }

    @Test
    void inputUnionInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = [true,false]"""));
    }

    @Test
    void inputUnionInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = [null]"""));
    }

    @Test
    void inputUnionInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = [{}, {}]"""));
    }

    @Test
    void inputUnionInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = [object(), object()]"""));
    }

    @Test
    void inputUnionInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = [object({}), object({})]"""));
    }

    @Test
    void inputUnionInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = [object, object]"""));
    }

    @Test
    void inputUnionInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] something = [{env: 'dev'}, {env: 'dev'}]"""));
    }


}
