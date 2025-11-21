package io.kite.semantics;

import io.kite.base.CheckerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Output")
public class OutputErrorsTest extends CheckerTest {
    /// STRING
    @Test
    void outputStringInitError() {
        assertThrows(TypeError.class, () -> eval("output string something = 10"));
    }

    @Test
    void outputStringInitDecimalError() {
        assertThrows(TypeError.class, () -> eval("output string something = 0.1"));
    }

    @Test
    void outputStringInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("output string something = true"));
    }

    @Test
    void outputStringInitNullError() {
        assertThrows(TypeError.class, () -> eval("output string something = null"));
    }

    @Test
    void outputStringInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("output string something = {}"));
    }

    @Test
    void outputStringInitObjectEmptyKeywordError() {
        assertThrows(TypeError.class, () -> eval("output string something = object()"));
    }

    @Test
    void outputStringInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("output string something = object({})"));
    }

    @Test
    void outputStringInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output string something = object({ env: 'dev'})"));
    }

    @Test
    void outputStringInitObjectError() {
        assertThrows(TypeError.class, () -> eval("output string something = { env: 'dev' }"));
    }

    @Test
    void outputStringDefaultEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("output string something = []"));
    }

    @Test
    void outputStringInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("output string something = ['hello']"));
    }

    @Test
    void outputStringInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("output string something = [1,2,3]"));
    }

    @Test
    void outputStringInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("output string something = [true,false]"));
    }

    @Test
    void outputStringInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("output string something = [null]"));
    }

    @Test
    void outputStringInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("output string something = [{}, {}]"));
    }

    @Test
    void outputStringInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("output string something = [object(), object()]"));
    }

    @Test
    void outputStringInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("output string something = [object({}), object({})]"));
    }

    @Test
    void outputStringInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output string something = [object, object]"));
    }

    @Test
    void outputStringInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output string something = [{env: 'dev'}, {env: 'dev'}]"));
    }


    /// NUMBER
    @Test
    void outputNumberInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("output number something = true"));
    }

    @Test
    void outputNumberInitStringError() {
        assertThrows(TypeError.class, () -> eval("output number something = 'hello'"));
    }

    @Test
    void outputNumberInitNullError() {
        assertThrows(TypeError.class, () -> eval("output number something = null"));
    }

    @Test
    void outputNumberInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("output number something = {}"));
    }

    @Test
    void outputNumberInitObjectEmptyKeywordError() {
        assertThrows(TypeError.class, () -> eval("output number something = object()"));
    }

    @Test
    void outputNumberInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("output number something = object({})"));
    }

    @Test
    void outputNumberInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output number something = object({ env: 'dev'})"));
    }

    @Test
    void outputNumberInitObjectError() {
        assertThrows(TypeError.class, () -> eval("output number something = { env: 'dev' }"));
    }


    @Test
    void outputNumberInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("output number something = ['hello']"));
    }

    @Test
    void outputNumberInitEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("output number something = []"));
    }

    @Test
    void outputNumberInitWithBlankArrayError() {
        assertThrows(TypeError.class, () -> eval("output number something = [      ] "));
    }

    @Test
    void outputNumberInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("output number something = [1,2,3]"));
    }

    @Test
    void outputNumberInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("output number something = [true,false]"));
    }

    @Test
    void outputNumberInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("output number something = [null]"));
    }

    @Test
    void outputNumberInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("output number something = [{}, {}]"));
    }

    @Test
    void outputNumberInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("output number something = [object(), object()]"));
    }

    @Test
    void outputNumberInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("output number something = [object({}), object({})]"));
    }

    @Test
    void outputNumberInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output number something = [object, object]"));
    }

    @Test
    void testOutputNumberInitWithTrueError() {
        assertThrows(TypeError.class, () -> eval("output number something = true"));
    }

    @Test
    void testOutputNumberInitWithFalseError() {
        assertThrows(TypeError.class, () -> eval("output number something = false"));
    }

    @Test
    void outputNumberInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output number something = [{env: 'dev'}, {env: 'dev'}]"));
    }

    /// BOOLEAN

    @Test
    void outputBooleanInitNullError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = null"));
    }

    @Test
    void outputBooleanInitStringError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = 'hello'"));
    }

    @Test
    void outputBooleanInitNumberError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = 10"));
    }

    @Test
    void outputBooleanInitDecimalError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = 0.1"));
    }

    @Test
    void outputBooleanDefaultEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = []"));
    }

    @Test
    void outputBooleanInitIntError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [1, 2, 3]"));
    }

    @Test
    void outputBooleanInitDecimalArrayError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [1.2, 2.3, 3.4]"));
    }

    @Test
    void outputBooleanInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = {}"));
    }

    @Test
    void outputBooleanInitObjectEmptyKeywordError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = object()"));
    }


    @Test
    void outputBooleanInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = object({})"));
    }

    @Test
    void outputBooleanInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = object({ env: 'dev'})"));
    }

    @Test
    void outputBooleanInitObjectError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = { env: 'dev' }"));
    }


    @Test
    void outputBooleanInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = ['hello']"));
    }

    @Test
    void outputBooleanInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [1, 2, 3]"));
    }

    @Test
    void outputBooleanInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [true,false]"));
    }

    @Test
    void outputBooleanInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [null]"));
    }

    @Test
    void outputBooleanInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [{}, {}]"));
    }

    @Test
    void outputBooleanInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [object(), object()]"));
    }

    @Test
    void outputBooleanInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [object({}), object({})]"));
    }

    @Test
    void outputBooleanInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [object, object]"));
    }

    @Test
    void outputBooleanInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output boolean something = [{env: 'dev'}, {env: 'dev'}]"));
    }

    /// OBJECT
    @Test
    void outputObjectInitNullError() {
        assertThrows(TypeError.class, () -> eval("output object something = null"));
    }

    @Test
    void outputObjectInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("output object something = true"));
    }

    @Test
    void outputObjectInitStringError() {
        assertThrows(TypeError.class, () -> eval("output object something = 'hello'"));
    }

    @Test
    void outputObjectInitNumberError() {
        assertThrows(TypeError.class, () -> eval("output object something = 10"));
    }

    @Test
    void outputObjectInitDecimalError() {
        assertThrows(TypeError.class, () -> eval("output object something = 0.1"));
    }

    @Test
    void outputObjectInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("output object something = [1,2,3]"));
    }

    @Test
    void outputObjectInitDecimalArrayError() {
        assertThrows(TypeError.class, () -> eval("output object something = [1.2,2.3,3.4]"));
    }

    @Test
    void outputObjectInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("output object something = ['hello']"));
    }

    @Test
    void outputObjectInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("output object something = [true,false]"));
    }

    @Test
    void outputObjectInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("output object something = [null]"));
    }

    @Test
    void outputObjectInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("output object something = [{}, {}]"));
    }

    @Test
    void outputObjectInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("output object something = [object(), object()]"));
    }

    @Test
    void outputObjectInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("output object something = [object({}), object({})]"));
    }

    @Test
    void outputObjectInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output object something = [object, object]"));
    }

    @Test
    void outputObjectInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("output object something = [{env: 'dev'}, {env: 'dev'}]"));
    }

    /// UNION
    @Test
    void outputUnionInitNullError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = null
                """));
    }

    @Test
    void outputMixedAliasDefaultNumberError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string
                output custom something = true
                """));
    }

    @Test
    void outputStringAliasDefaultNumberError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom something = 10
                """));
    }

    @Test
    void outputStringAliasDefaultDecimalError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom something = 10.1
                """));
    }

    @Test
    void outputStringAliasDefaultDecimalDotError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom something = 0.1
                """));
    }

    @Test
    void outputStringAliasDefaultBooleanError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom something = true
                """));
    }

    @Test
    void testOutputNumberAliasInitWithBooleanError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = true
                """));
    }


    @Test
    void testOutputNumberAliasInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = []
                """));
    }

    @Test
    void testOutputNumberAliasInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = ['hello','world']
                """));
    }

    @Test
    void testOutputNumberAliasInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = [1,2,3]
                """));
    }

    @Test
    void testOutputNumberAliasInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = [true, false]
                """));
    }

    @Test
    void testOutputNumberAliasInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = [{ env : 'dev' }]
                """));
    }

    @Test
    void testOutputNumberAliasInitWithObjectArrayKeyStringError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = [{ 'env' : 'dev' }]
                """));
    }

    @Test
    void outputUnionInitBooleanError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = true
                """));
    }

    @Test
    void outputUnionInitIntError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [1,2,3]
                """));
    }

    @Test
    void outputUnionInitDecimalError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [1.2,2.3,3.4]
                """));
    }

    @Test
    void outputUnionInitObjectEmptyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = {}
                """));
    }

    @Test
    void outputUnionInitObjectEmptyKeywordError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = object()
                """));
    }

    @Test
    void outputUnionInitObjectEmptyKeywordExtraSpaceError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = object()
                
                """));
    }

    @Test
    void outputUnionInitObjectEmptyKeywordNoBodyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = object()
                """));
    }

    @Test
    void outputUnionInitObjectEmptyKeywordEmptyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = object({})
                """));
    }

    @Test
    void outputUnionInitObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = object({ env: 'dev'})
                """));
    }

    @Test
    void outputUnionInitObjectError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = { env: 'dev' }
                """));
    }


    @Test
    void outputUnionInitArrayStringError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = ['hello']
                """));
    }

    @Test
    void outputUnionInitArrayNumberError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [1,2,3]
                """));
    }

    @Test
    void outputUnionInitArrayBooleanError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [true,false]
                """));
    }

    @Test
    void outputUnionInitArrayNullError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [null]
                """));
    }

    @Test
    void outputUnionInitArrayEmptyObjectError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [{}, {}]
                """));
    }

    @Test
    void outputUnionInitArrayEmptyObjectBodyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [object(), object()]
                """));
    }

    @Test
    void outputUnionInitArrayEmptyObjectBodyEmptyError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [object({}), object({})]
                """));
    }

    @Test
    void outputUnionInitArrayEmptyObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [object, object]
                """));
    }

    @Test
    void outputUnionInitArrayObjectKeywordError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                output custom something = [{env: 'dev'}, {env: 'dev'}]
                """));
    }

    @Test
    void testOutputArrayInitWithBooleanError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = true"));
    }

    @Test
    void testOutputArrayInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = ['hello','world']"));
    }

    @Test
    void testOutputArrayInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = [1,2,3]")); // throw because it's not declared as array
    }

    @Test
    void testOutputArrayInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = [true, false]"));
    }

    @Test
    void testOutputArrayInitWithStringError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = 'hello' "));
    }

    @Test
    void testOutputArrayInitWithDecimalError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = 1.2"));
    }

    @Test
    void testOutputArrayInitWithDecimalDotError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = 0.2"));
    }

    @Test
    void testOutputArrayInitWithNumberError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = 123"));
    }

}
