package cloud.kitelang.semantics.typechecker;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeEnvironment;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.ArrayType;
import cloud.kitelang.semantics.types.ObjectType;
import cloud.kitelang.semantics.types.StringType;
import cloud.kitelang.semantics.types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker val array")
@Disabled
public class ValArrayTest extends CheckerTest {

    @Test
    void testVarEmptyArray() {
        eval("""
                val x = []
                """);
        var varType = checker.getEnv().lookup("x");
        Assertions.assertInstanceOf(ArrayType.class, varType);
    }

    @Test
    void testNumber() {
        eval("""
                val x = [1, 2]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testDecimal() {
        eval("""
                val x = [1.1, 2.2]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testBoolean() {
        eval("""
                val x = [true,false]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Boolean);
    }

    @Test
    void testObject() {
        eval("""
                val x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), new ObjectType(new TypeEnvironment(Map.of("env", StringType.String))));
    }

    @Test
    void testDeclareTypeNumber() {
        eval("""
                val number[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testDeclareTypeString() {
        eval("""
                val string[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.String);
    }

    @Test
    void testDeclareTypeBoolean() {
        eval("""
                val boolean[] x = []
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Boolean);
    }

    @Test
    void testDeclareTypeNumberInit() {
        eval("""
                val number[] x = [1,2,3]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Number);
    }

    @Test
    void testDeclareTypeStringInit() {
        eval("""
                val string[] x = ["hi",'hello']
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.String);
    }

    @Test
    void testDeclareTypeStringInitWrong() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val string[] x = [1]
                """));
    }

    @Test
    void testDeclareTypeStringInitWrongbool() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val string[] x = [true]
                """));
    }

    @Test
    void testDeclareTypeBoolInitWrong() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val boolean[] x = [1]
                """));
    }

    @Test
    void testDeclareTypeBoolInitWrongString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val boolean[] x = ['hi']
                """));
    }

    @Test
    void testDeclareTypeBooleanInit() {
        eval("""
                val boolean[] x = [true]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ValueType.Boolean);
    }

    @Test
    void testDeclareTypeObjectInit() {
        eval("""
                val object[] x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(varType.getType(), ObjectType.INSTANCE);
    }


    @Test
    void testReassign() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        val boolean[] x = []
                        x=[]
                        """)
        );
    }

    @Test
    void testAppend() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        val boolean[] x = []
                        x += [true]
                        """)
        );
    }

    @Test
    void testReassignNumber() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        val number[] x = []
                        x=[]
                        """)
        );
    }

    @Test
    void testAppendNumber() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        val number[] x = []
                        x += [1]
                        """)
        );
    }

}
