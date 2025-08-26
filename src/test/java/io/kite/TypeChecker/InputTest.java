package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.ObjectType;
import io.kite.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.TypeChecker.Types.ArrayType.arrayType;
import static io.kite.TypeChecker.Types.UnionType.unionType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void inputStringInitError() {
        assertThrows(TypeError.class, () -> eval("input string something = 10"));
    }

    @Test
    void inputNumberInitError() {
        assertThrows(TypeError.class, () -> eval("input number something = 'hello' "));
    }

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
