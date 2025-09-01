package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.AnyType;
import io.kite.TypeChecker.Types.ObjectType;
import io.kite.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.TypeChecker.Types.ArrayType.arrayType;
import static io.kite.TypeChecker.Types.UnionType.unionType;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker Input")
public class InputTest extends CheckerTest {

    @Test
    void inputString() {
        var res = eval("input string something");
        assertEquals(ValueType.String, res);
    }

    @Test
    void inputNumber() {
        var res = eval("input number something");
        assertEquals(ValueType.Number, res);
    }

    @Test
    void inputBoolean() {
        var res = eval("input boolean something");
        assertEquals(ValueType.Boolean, res);
    }

    @Test
    void inputObject() {
        var res = eval("input object something");
        assertEquals(ObjectType.INSTANCE, res);
    }

    @Test
    void inputUnion() {
        var res = eval("""
                type custom = string | number
                input custom something
                """);
        assertEquals(unionType("custom", ValueType.String, ValueType.Number), res);
    }


    @Test
    void inputAnyInitNull() {
        var res = eval("input any something = null");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitObjectEmpty() {
        var res = eval("input any something = {}");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitObjectEmptyKeyword() {
        var res = eval("input any something = object");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitObjectEmptyKeywordNoBody() {
        var res = eval("input any something = object()");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitObjectEmptyKeywordEmpty() {
        var res = eval("input any something = object({})");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitObjectKeyword() {
        var res = eval("input any something = object({ env: 'dev'})");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitObject() {
        var res = eval("input any something = { env: 'dev' }");
        assertEquals(AnyType.INSTANCE, res);
    }


    @Test
    void inputAnyInitArrayString() {
        var res = eval("input any something = ['hello']");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitArrayNumber() {
        var res = eval("input any something = [1,2,3]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitArrayBoolean() {
        var res = eval("input any something = [true,false]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitArrayNull() {
        var res = eval("input any something = [true,false]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitArrayEmptyObject() {
        var res = eval("input any something = [{}, {}]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitArrayEmptyObjectBody() {
        var res = eval("input any something = [object(), object()]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitArrayEmptyObjectBodyEmpty() {
        var res = eval("input any something = [object({}), object({})]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitArrayEmptyObjectKeyword() {
        var res = eval("input any something = [object, object]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputAnyInitArrayObjectKeyword() {
        var res = eval("input any something = [{env: 'dev'}, {env: 'dev'}]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void inputStringInit() {
        var res = eval("input string something = 'something'");
        assertEquals(ValueType.String, res);
    }

    @Test
    void inputNumberInit() {
        var res = eval("input number something = 10 ");
        assertEquals(ValueType.Number, res);
    }

    @Test
    void inputBooleanInit() {
        var res = eval("input boolean something = true");
        assertEquals(ValueType.Boolean, res);
    }

    @Test
    void inputObjectInitEmpty() {
        var res = eval("input object something = {}");
        assertEquals(ObjectType.INSTANCE, res);
    }

    @Test
    void inputObjectInit() {
        var res = eval("input object something = {env : 'dev'}");
        assertEquals(ObjectType.INSTANCE, res);
    }

    @Test
    void inputUnionInit() {
        var res = eval("""
                type custom = string | number
                input custom something = 10
                """);
        assertEquals(unionType("custom", ValueType.String, ValueType.Number), res);
    }

    @Test
    void inputStringArray() {
        var res = eval("input string[] something");
        assertEquals(arrayType(ValueType.String), res);
    }

    @Test
    void inputNumberArray() {
        var res = eval("input number[] something");
        assertEquals(arrayType(ValueType.Number), res);
    }

    @Test
    void inputBooleanArray() {
        var res = eval("input boolean[] something");
        assertEquals(arrayType(ValueType.Boolean), res);
    }

    @Test
    void inputObjectArray() {
        var res = eval("input object[] something");
        assertEquals(arrayType(ObjectType.INSTANCE), res);
    }

    @Test
    void inputUnionArray() {
        var res = eval("""
                type custom = string | number
                input custom[] something
                """);

        assertEquals(arrayType(unionType("custom", ValueType.String, ValueType.Number)), res);
    }

    @Test
    void inputStringArrayInit() {
        var res = eval("input string[] something=['hi']");
        assertEquals(arrayType(ValueType.String), res);
    }

    @Test
    void inputNumberArrayInit() {
        var res = eval("input number[] something=[1,2,3]");
        assertEquals(arrayType(ValueType.Number), res);
    }

    @Test
    void inputBooleanArrayInit() {
        var res = eval("input boolean[] something=[true,false,true]");
        assertEquals(arrayType(ValueType.Boolean), res);
    }

    @Test
    void inputObjectArrayInit() {
        var res = eval("input object[] something=[{env:'dev'}]");
        assertEquals(arrayType(ObjectType.INSTANCE), res);
    }


    @Test
    void inputUnionArrayInit() {
        var res = eval("""
                type custom = string | number
                input custom[] something = [10]
                """);
        assertEquals(arrayType(unionType("custom", ValueType.String, ValueType.Number)), res);
    }


}
