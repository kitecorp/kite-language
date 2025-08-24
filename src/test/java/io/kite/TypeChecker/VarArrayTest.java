package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker var array")
public class VarArrayTest extends CheckerTest {

    @Test
    void testVarEmptyArray() {
        eval("""
                var x = []
                """);
        var varType = checker.getEnv().lookup("x");
        Assertions.assertInstanceOf(ArrayType.class, varType);
    }

    @Test
    void testNumber() {
        eval("""
                var x = [1, 2]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testDecimal() {
        eval("""
                var x = [1.1, 2.2]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testBoolean() {
        eval("""
                var x = [true,false]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Boolean, varType.getType());
    }

    @Test
    void testObject() {
        eval("""
                var x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(new ObjectType(new TypeEnvironment(Map.of("env", StringType.String))), varType.getType());
    }

    @Test
    void testDeclareTypeNumber() {
        eval("""
                var number[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testDeclareTypeString() {
        eval("""
                var string[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void testDeclareTypeBoolean() {
        eval("""
                var boolean[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Boolean, varType.getType());
    }

    @Test
    void testDeclareTypeAny() {
        eval("""
                var any[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testDeclareTypeNumberInit() {
        eval("""
                var number[] x = [1,2,3]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testDeclareTypeStringInit() {
        eval("""
                var string[] x = ["hi",'hello']
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void testAnyString() {
        eval("""
                var any[] x = ["hi",'hello']
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testAnyNumbers() {
        eval("""
                var any[] x = [1,2,3]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testAnyBoolean() {
        eval("""
                var any[] x = [true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testAnyNull() {
        eval("""
                var any[] x = ['str', null]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testAnyObject() {
        eval("""
                var any[] x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    /**
     * If any of the elements in the array is different than the first element then the type is of the array is ANY.
     */
    @Test
    void testTypeAny() {
        eval("""
                var x = ["hi", 1, true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testDeclareTypeStringInitWrong() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var string[] x = [1]
                """));
    }

    @Test
    void testDeclareTypeStringInitWrongEmptyObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var string[] x = [{}]
                """));
    }

    @Test
    void testDeclareTypeStringInitWrongObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var string[] x = [{env: "prod"}]
                """));
    }

    @Test
    void testDeclareTypeStringInitWrongbool() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var string[] x = [true]
                """));
    }

    @Test
    void testDeclareTypeBoolInitWrong() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var boolean[] x = [1]
                """));
    }

    @Test
    void testDeclareTypeBoolInitWrongString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var boolean[] x = ['hi']
                """));
    }

    @Test
    void testDeclareTypeBooleanInit() {
        eval("""
                var boolean[] x = [true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Boolean, varType.getType());
    }

    @Test
    void testDeclareTypeObjectInit() {
        eval("""
                var object[] x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ObjectType.INSTANCE, varType.getType());
    }


    @Test
    void testReassign() {
        eval("""
                var boolean[] x = []
                x=[true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Boolean, varType.getType());
    }

    @Test
    void testReassignNumber() {
        eval("""
                var number[] x = []
                x=[1]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testReassignStringSingleQuote() {
        eval("""
                var string[] x = ['hi']
                x=['hello']
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void testReassignString() {
        eval("""
                var string[] x = ["hi"]
                x=["hello"]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void testReassignObject() {
        eval("""
                var object[] x = [{env: 'prod'}]
                x=[{env: 'dev'}]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ObjectType.INSTANCE, varType.getType());
    }

    @Test
    void testReassignAny() {
        eval("""
                var any[] x = [1,2,3]
                x=[3]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testReassignAnyString() {
        eval("""
                var any[] x = ['hello']
                x=['world']
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testReassignAnyBoolean() {
        eval("""
                var any[] x = [true]
                x=[false]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testReassignAnyEmpty() {
        eval("""
                var any[] x = [true]
                x=[]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testReassignNumberToString() {
        eval("""
                var any[] x = [1]
                x=['hello']
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void testAppend() {
        eval("""
                var boolean[] x = []
                x += [true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Boolean, varType.getType());
    }

    @Test
    void testAppendNumberArray() {
        eval("""
                var number[] x = []
                x += [1]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testAppendAnotherArray() {
        eval("""
                var boolean[] x = []
                var boolean[] y = [false]
                x += y
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Boolean, varType.getType());
    }

    @Test
    void testAppendAnotherNumberArray() {
        eval("""
                var number[] x = []
                var number[] y = [1]
                x += y
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testAppendNumber() {
        eval("""
                var number[] x = []
                x += 1
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testAppendTrue() {
        eval("""
                var boolean[] x = []
                x += true
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Boolean, varType.getType());
    }

    @Test
    void testAppendFalse() {
        eval("""
                var boolean[] x = []
                x += false
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Boolean, varType.getType());
    }

    @Test
    void testAppendString() {
        eval("""
                var string[] x = []
                x += "hello"
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void testAppendStringSingleQuote() {
        eval("""
                var string[] x = []
                x += 'hello'
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void testAppendNull() {
        eval("""
                var string[] x = []
                x += null
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getType());
    }


    @Test
    void testAppendWrongType() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var number[] x = []
                x += [true]
                """));
    }

    @Test
    void appendNumberArray() {
        eval("""
                var x = ["hi", 1, true]
                x += [2]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }

    @Test
    void appendStringArray() {
        eval("""
                var x = ["hi", 1, true]
                x += [2]
                x += ["world"]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(AnyType.INSTANCE, varType.getType());
    }


}
