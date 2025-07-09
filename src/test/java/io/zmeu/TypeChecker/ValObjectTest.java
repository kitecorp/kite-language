package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.ObjectType;
import io.zmeu.TypeChecker.Types.ReferenceType;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parse.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parse.Literals.ObjectLiteral.object;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeChecker Object")
public class ValObjectTest extends CheckerTest {

    @Test
    void testVarInt() {
        var xType = checker.visit(object("env", number(10)));
        assertEquals(xType, ValueType.Number);
    }

    @Test
    void testVarString() {
        eval("""
                val x = { "env": "hello" }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("env"), ValueType.String);
    }

    @Test
    void testVarNumber() {
        eval("""
                val x = { "env": 2 }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("env"), ValueType.Number);
    }

    @Test
    void testVarDecimal() {
        eval("""
                val x = { "env": 2.1 }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("env"), ValueType.Number);
    }

    @Test
    void testVarBoolean() {
        eval("""
                val x = { "env": false }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("env"), ValueType.Boolean);
    }

    @Test
    void testVarNull() {
        eval("""
                val x = { "env": null }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("env"), ValueType.Null);
    }

    @Test
    void testVarDeclareType() {
        eval("""
                val object x = { "env": "prod" }
                """);
        var varType = (ReferenceType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("env"), ValueType.String);
    }

    @Test
    void testNestedObject() {
        eval("""
                val x = {
                  "env": {
                    "name": "prod",
                    "settings": {
                      "verbose": false,
                      "retries": 3
                    }
                  }
                }
                """);
        ObjectType xt = (ObjectType) checker.getEnv().lookup("x");
        // top-level “env” is an object
        var envType = xt.getProperty("env");
        assertInstanceOf(ObjectType.class, envType);
        ObjectType envObj = (ObjectType) envType;
        assertEquals(envObj.getProperty("name"), ValueType.String);
        ObjectType settings = (ObjectType) envObj.getProperty("settings");
        assertEquals(settings.getProperty("verbose"), ValueType.Boolean);
        assertEquals(settings.getProperty("retries"), ValueType.Number);
    }

    @Test
    void testReferenceAlias() {
        eval("""
                    val x = { "count": 1 };
                    var y = x;
                """);
        // Both x and y should reflect the same object reference
        var yType = (ObjectType) checker.getEnv().lookup("y");
        assertEquals(ValueType.Number, yType.getProperty("count"));
    }

    @Test
    void testMissingProperty() {
        assertThrows(TypeError.class, () ->
                eval("""
                        val x = { "foo": 42 };
                        x.bar;                  // bar doesn’t exist
                        """)
        );
    }

    @Test
    @DisplayName("Missing Nested Property")
    void testMissingNestedProperty() {
        assertThrows(TypeError.class, () ->
                eval("""
                        val x = { "a": { "b": 1 } };
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
                        val x = { "a": { "b": { "c": true } } };
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
                        val key = "env";
                        val x = { "foo": 1 };
                        x[key];         // “env” isn’t defined on x
                        """
                )
        );
    }

    @Test
    @DisplayName("Computed Property Key Present")
    void testMissingComputedKey2() {
        eval("""
                val key = "env";
                val x = { "env": 1 };
                x[key];         // “env” is defined on x
                """
        );
        var yType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, yType.getProperty("env"));
    }

    @Test
    @DisplayName("Computed Property Key Present")
    void testMissingComputedKeyString() {
        eval("""
                val key = "env";
                val x = { "env": 1 };
                x["key"];         // “key” is missing
                """
        );
        var yType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, yType.getProperty("env"));
    }

    @Test
    @DisplayName("Reassign Existing Property with Wrong Type")
    void testPropertyReassignmentTypeError() {
        assertThrows(TypeError.class, () ->
                eval("""
                        val x = { "env": 1 };
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
                        val object x = { "a": 1 };
                        x.b = 2;               // adding 'b' not allowed if strict
                        """
                )
        );
    }

    @Test
    @DisplayName("Cannot Reassign Val Binding")
    void testValRebindError() {
        assertThrows(TypeError.class, () ->
                eval("""
                        val x = { "a": 1 };
                        x = { "a": 2 };       // rebinding x not allowed
                        """
                )
        );
    }

    @Test
    @DisplayName("Deep immutability: direct property mutation error")
    void testValPropertyMutationError() {
        assertThrows(TypeError.class, () -> eval("""
                        val x = { "count": 0 };
                        x.count = 5;           // forbidden on a val object
                        """
                )
        );
    }

    @Test
    @DisplayName("Deep immutability: direct nested property mutation error")
    void testValNestedPropertyMutationError() {
        assertThrows(TypeError.class, () -> eval("""
                        val x = { 
                            count: { 
                                eggs: 2
                            }
                        }
                        x.count.eggs = 5;           // forbidden on a val object
                        """
                )
        );
    }

    @Test
    @DisplayName("Deep immutability: alias property mutation error")
    void testValAliasPropertyMutationError() {
        assertThrows(TypeError.class, () ->
                eval("""
                        val x = { "foo": 1 };
                        var y = x;
                        y.foo = 2;             // still forbidden, object is frozen
                        """
                )
        );
    }

    @Test
    @DisplayName("Var alias rebind allowed")
    void testVarAliasRebindOk() {
        // even though the object is frozen, `y` is a var binding so you may point it elsewhere
        eval("""
                val x = { "a": 1 };
                var y = x;
                y = { "a": 2 };         // OK, rebind y to a new object
                """);
        ObjectType yT = (ObjectType) checker.getEnv().lookup("y");
        assertEquals(ValueType.Number, yT.getProperty("a"));
    }

}
