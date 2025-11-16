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

@DisplayName("TypeChecker Output")
public class OutputTest extends CheckerTest {

    @Test
    void outputUnion() {
        var res = eval("""
                type custom = string | number
                output custom something
                """);
        assertEquals(unionType("custom", ValueType.String, ValueType.Number), res);
    }


    @Test
    void outputAnyInitNull() {
        var res = eval("output any something = null");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitObjectEmpty() {
        var res = eval("output any something = {}");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitObjectEmptyKeyword() {
        var res = eval("output any something = object()");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitObjectEmptyKeywordEmpty() {
        var res = eval("output any something = object({})");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitObjectKeyword() {
        var res = eval("output any something = object({ env: 'dev'})");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitObject() {
        var res = eval("output any something = { env: 'dev' }");
        assertEquals(AnyType.INSTANCE, res);
    }


    @Test
    void outputAnyInitArrayString() {
        var res = eval("output any something = ['hello']");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitArrayNumber() {
        var res = eval("output any something = [1,2,3]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitArrayBoolean() {
        var res = eval("output any something = [true,false]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitArrayNull() {
        var res = eval("output any something = [true,false]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitArrayEmptyObject() {
        var res = eval("output any something = [{}, {}]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitArrayEmptyObjectBody() {
        var res = eval("output any something = [object(), object()]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitArrayEmptyObjectBodyEmpty() {
        var res = eval("output any something = [object({}), object({})]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitArrayEmptyObjectKeyword() {
        var res = eval("output any something = [object, object]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputAnyInitArrayObjectKeyword() {
        var res = eval("output any something = [{env: 'dev'}, {env: 'dev'}]");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void outputStringInit() {
        var res = eval("output string something = 'something'");
        assertEquals(ValueType.String, res);
    }

    @Test
    void outputNumberInit() {
        var res = eval("output number something = 10 ");
        assertEquals(ValueType.Number, res);
    }

    @Test
    void outputBooleanInit() {
        var res = eval("output boolean something = true");
        assertEquals(ValueType.Boolean, res);
    }

    @Test
    void outputObjectInitEmpty() {
        var res = eval("output object something = {}");
        assertEquals(ObjectType.INSTANCE, res);
    }

    @Test
    void outputObjectInit() {
        var res = eval("output object something = {env : 'dev'}");
        assertEquals(ObjectType.INSTANCE, res);
    }

    @Test
    void outputUnionInit() {
        var res = eval("""
                type custom = string | number
                output custom something = 10
                """);
        assertEquals(unionType("custom", ValueType.String, ValueType.Number), res);
    }

    @Test
    void outputStringArrayInit() {
        var res = eval("output string[] something=['hi']");
        assertEquals(arrayType(ValueType.String), res);
    }

    @Test
    void outputNumberArrayInit() {
        var res = eval("output number[] something=[1,2,3]");
        assertEquals(arrayType(ValueType.Number), res);
    }

    @Test
    void outputBooleanArrayInit() {
        var res = eval("output boolean[] something=[true,false,true]");
        assertEquals(arrayType(ValueType.Boolean), res);
    }

    @Test
    void outputObjectArrayInit() {
        var res = eval("output object[] something=[{env:'dev'}]");
        assertEquals(arrayType(ObjectType.INSTANCE), res);
    }


    @Test
    void outputUnionArrayInit() {
        var res = eval("""
                type custom = string | number
                output custom[] something = [10]
                """);
        assertEquals(arrayType(unionType("custom", ValueType.String, ValueType.Number)), res);
    }


}
