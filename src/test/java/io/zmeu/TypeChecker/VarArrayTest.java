package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    void testVarDeclareType() {
        eval("""
                var object x = { "env": "prod" }
                """);
        var varType = (ReferenceType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("env"), ValueType.String);
    }

    @Test
    void testNestedObject() {
        eval("""
                var x = {
                  "env": {
                    "name": "prod",
                    "settings": {
                      "verbose": false,
                      "retries": 3
                    }
                  }
                }
                """);
        ArrayType xt = (ArrayType) checker.getEnv().lookup("x");
        // top-level “env” is an object
        var envType = xt.getProperty("env");
        assertInstanceOf(ArrayType.class, envType);
        ArrayType envObj = (ArrayType) envType;
        assertEquals(envObj.getProperty("name"), ValueType.String);
        ArrayType settings = (ArrayType) envObj.getProperty("settings");
        assertEquals(settings.getProperty("verbose"), ValueType.Boolean);
        assertEquals(settings.getProperty("retries"), ValueType.Number);
    }

    @Test
    void testReferenceAlias() {
        eval("""
                        var x = { "count": 1 };
                        var y = x;
                        x.count = 2;
                """);
        // Both x and y should reflect the same object reference
        var yType = (ArrayType) checker.getEnv().lookup("y");
        assertEquals(ValueType.Number, yType.getProperty("count"));
    }

    @Test
    void testMissingProperty() {
        assertThrows(TypeError.class, () ->
                eval("""
                        var x = { "foo": 42 };
                        x.bar;                  // bar doesn’t exist
                        """)
        );
    }

    @Test
    @DisplayName("Missing Nested Property")
    void testMissingNestedProperty() {
        assertThrows(TypeError.class, () ->
                eval("""
                        var x = { "a": { "b": 1 } };
                        x.a.c;          // “c” isn’t on the nested object
                        """
                )
        );
    }

    @Test
    @DisplayName("Missing Deeply Nested Property")
    void testMissingDeeplyNestedProperty() {
        assertThrows(TypeError.class, () ->
                eval("""
                        var x = { "a": { "b": { "c": true } } };
                        x.a.b.d;        // “d” isn’t on the deepest object
                        """
                )
        );
    }

    @Test
    @DisplayName("Computed Property Key Missing")
    void testMissingComputedKey() {
        assertThrows(TypeError.class, () ->
                eval("""
                        var key = "env";
                        var x = { "foo": 1 };
                        x[key];         // “env” isn’t defined on x
                        """
                )
        );
    }

    @Test
    @DisplayName("Computed Property Key Present")
    void testMissingComputedKey2() {
        eval("""
                var key = "env";
                var x = { "env": 1 };
                x[key];         // “env” is defined on x
                """
        );
        var yType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, yType.getProperty("env"));
    }

    @Test
    @DisplayName("Computed Property Key Present")
    void testMissingComputedKeyString() {
        eval("""
                var key = "env";
                var x = { "env": 1 };
                x["key"];         // “key” is missing
                """
        );
        var yType = (ArrayType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, yType.getProperty("env"));
    }

    @Test
    @DisplayName("Reassign Existing Property with Wrong Type")
    void testPropertyReassignmentTypeError() {
        assertThrows(TypeError.class, () ->
                eval("""
                        var x = { "env": 1 };
                        x.env = "oops";        // was Number, now String
                        """
                )
        );
    }

    @Test
    @DisplayName("Add New Property After Declaration")
    void testAddNewProperty() {
        assertThrows(TypeError.class, () ->
                eval("""
                        var object x = { "a": 1 };
                        x.b = 2;               // adding 'b' not allowed if strict
                        """
                )
        );
    }

}
