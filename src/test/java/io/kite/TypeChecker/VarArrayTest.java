package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.ArrayType;
import io.kite.TypeChecker.Types.ObjectType;
import io.kite.TypeChecker.Types.StringType;
import io.kite.TypeChecker.Types.ValueType;
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
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testDecimal() {
        eval("""
                var x = [1.1, 2.2]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testBoolean() {
        eval("""
                var x = [true,false]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Boolean);
    }

    @Test
    void testObject() {
        eval("""
                var x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), new ObjectType(new TypeEnvironment(Map.of("env", StringType.String))));
    }

    @Test
    void testDeclareTypeNumber() {
        eval("""
                var number[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testDeclareTypeString() {
        eval("""
                var string[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.String);
    }

    @Test
    void testDeclareTypeBoolean() {
        eval("""
                var boolean[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Boolean);
    }

    @Test
    void testDeclareTypeNumberInit() {
        eval("""
                var number[] x = [1,2,3]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testDeclareTypeStringInit() {
        eval("""
                var string[] x = ["hi",'hello']
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.String);
    }

    @Test
    void testTypeAny() {
        eval("""
                var x = ["hi", 1, true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.String);
    }

    @Test
    void testDeclareTypeStringInitWrong() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var string[] x = [1]
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
        assertEquals(varType.getType(), ValueType.Boolean);
    }

    @Test
    void testDeclareTypeObjectInit() {
        eval("""
                var object[] x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ObjectType.INSTANCE);
    }


    @Test
    void testReassign() {
        eval("""
                var boolean[] x = []
                x=[true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Boolean);
    }

    @Test
    void testAppend() {
        eval("""
                var boolean[] x = []
                x += [true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Boolean);
    }

    @Test
    void testReassignNumber() {
        eval("""
                var number[] x = []
                x=[1]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testAppendNumber() {
        eval("""
                var number[] x = []
                x += [1]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testAppendWrongType() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var number[] x = []
                x += [true]
                """));
    }

}
