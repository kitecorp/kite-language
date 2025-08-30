package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.ObjectType;
import io.kite.TypeChecker.Types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static io.kite.TypeChecker.Types.ArrayType.arrayType;
import static io.kite.TypeChecker.Types.UnionType.unionType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Log4j2
public class InputTest extends RuntimeTest {

    private InputStream sysInBackup = System.in;

    @AfterEach
    void cleanup() {
        System.setIn(sysInBackup);
    }

    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    private void setInput(Integer input) {
        setInput(input.toString());
    }

    private void setInput(Boolean input) {
        setInput(input.toString());
    }

    private void setInput(Double input) {
        setInput(input.toString());
    }

    private void setInput(Float input) {
        setInput(input.toString());
    }

    @Test
    void inputString() {
        setInput("hello");
        var res = eval("input string region");
        assertEquals("hello", res);
    }

    @Test
    void inputNumber() {
        setInput(10);
        var res = eval("input number region");
        assertEquals(10, res);
    }

    @Test
    void inputBoolean() {
        setInput(true);
        var res = eval("input boolean region");
        assertEquals(true, res);
    }

    @Test
    void inputObject() {
        var res = eval("input object region");
        assertEquals(ObjectType.INSTANCE, res);
    }

    @Test
    void inputUnion() {
        var res = eval("""
                type custom = string | number
                input custom region
                """);
        assertEquals(unionType("custom", ValueType.String, ValueType.Number), res);
    }

    @Test
    void inputStringInit() {
        var res = eval("input string region = 'region'");
        assertEquals(ValueType.String, res);
    }

    @Test
    void inputNumberInit() {
        var res = eval("input number region = 10 ");
        assertEquals(ValueType.Number, res);
    }

    @Test
    void inputBooleanInit() {
        var res = eval("input boolean region = true");
        assertEquals(ValueType.Boolean, res);
    }

    @Test
    void inputObjectInitEmpty() {
        var res = eval("input object region = {}");
        assertEquals(ObjectType.INSTANCE, res);
    }

    @Test
    void inputObjectInit() {
        var res = eval("input object region = {env : 'dev'}");
        assertEquals(ObjectType.INSTANCE, res);
    }

    @Test
    void inputUnionInit() {
        var res = eval("""
                type custom = string | number
                input custom region = 10
                """);
        assertEquals(unionType("custom", ValueType.String, ValueType.Number), res);
    }

    @Test
    void inputStringInitError() {
        assertThrows(TypeError.class, () -> eval("input string region = 10"));
    }

    @Test
    void inputNumberInitError() {
        assertThrows(TypeError.class, () -> eval("input number region = 'hello' "));
    }

    @Test
    void inputBooleanInitError() {
        assertThrows(TypeError.class, () -> eval("input boolean region = 123"));
    }

    @Test
    void inputObjectInitEmptyError() {
        assertThrows(TypeError.class, () -> eval("input object region = true"));
    }

    @Test
    void inputObjectInitError() {
        assertThrows(TypeError.class, () -> eval("input object region = 'hello'"));
    }

    @Test
    void inputUnionInitError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom region = true
                """));
    }

    @Test
    void inputStringArray() {
        var res = eval("input string[] region");
        assertEquals(arrayType(ValueType.String), res);
    }

    @Test
    void inputNumberArray() {
        var res = eval("input number[] region");
        assertEquals(arrayType(ValueType.Number), res);
    }

    @Test
    void inputBooleanArray() {
        var res = eval("input boolean[] region");
        assertEquals(arrayType(ValueType.Boolean), res);
    }

    @Test
    void inputObjectArray() {
        var res = eval("input object[] region");
        assertEquals(arrayType(ObjectType.INSTANCE), res);
    }

    @Test
    void inputUnionArray() {
        var res = eval("""
                type custom = string | number
                input custom[] region
                """);

        assertEquals(arrayType(unionType("custom", ValueType.String, ValueType.Number)), res);
    }

    @Test
    void inputStringArrayInit() {
        var res = eval("input string[] region=['hi']");
        assertEquals(arrayType(ValueType.String), res);
    }

    @Test
    void inputNumberArrayInit() {
        var res = eval("input number[] region=[1,2,3]");
        assertEquals(arrayType(ValueType.Number), res);
    }

    @Test
    void inputBooleanArrayInit() {
        var res = eval("input boolean[] region=[true,false,true]");
        assertEquals(arrayType(ValueType.Boolean), res);
    }

    @Test
    void inputObjectArrayInit() {
        var res = eval("input object[] region=[{env:'dev'}]");
        assertEquals(arrayType(ObjectType.INSTANCE), res);
    }


    @Test
    void inputUnionArrayInit() {
        var res = eval("""
                type custom = string | number
                input custom[] region = [10]
                """);
        assertEquals(arrayType(unionType("custom", ValueType.String, ValueType.Number)), res);
    }

    @Test
    void inputStringArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input string[] region=[1,2,3]"));
    }

    @Test
    void inputNumberArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input number[] region=['hi']"));
    }

    @Test
    void inputBooleanArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] region=[1]"));
    }

    @Test
    void inputObjectArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input object[] region=[1]"));
    }


    @Test
    void inputUnionArrayInitError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] region = [true]
                """));
    }

}
