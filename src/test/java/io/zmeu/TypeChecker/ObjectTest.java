package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.ObjectType;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Literals.ObjectLiteral.object;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker Object")
public class ObjectTest extends CheckerTest {

    @Test
    void testVarInt() {
        var xType = checker.visit(object("env", number(10)));
        assertEquals(xType, ValueType.Number);
    }

    @Test
    void testVarString() {
        eval("""
                var x = { "env": "hello" }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("""
                "env"
                """.trim()), ValueType.String);
    }

    @Test
    void testVarNumber() {
        eval("""
                var x = { "env": 2 }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("""
                "env"
                """.trim()), ValueType.Number);
    }

    @Test
    void testVarDecimal() {
        eval("""
                var x = { "env": 2.1 }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("""
                "env"
                """.trim()), ValueType.Number);
    }

    @Test
    void testVarBoolean() {
        eval("""
                var x = { "env": false }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("""
                "env"
                """.trim()), ValueType.Boolean);
    }

    @Test
    void testVarNull() {
        eval("""
                var x = { "env": null }
                """);
        var varType = (ObjectType) checker.getEnv().lookup("x");
        assertEquals(varType.getProperty("""
                "env"
                """.trim()), ValueType.Null);
    }
//
//    @Test
//    void testVarExplicitType() {
//        var type = checker.visit(var("x", type("string"), string("hello")));
//        var accessType = checker.visit(id("x"));
//        assertEquals(type, ValueType.String);
//        assertEquals(accessType, ValueType.String);
//    }
//
//    @Test
//    void testVarExplicitTypeWrongNumberAssignment() {
//        assertThrows(TypeError.class, () -> checker.visit(var("x", type("string"), number(10))));
//        assertThrows(TypeError.class, () -> checker.visit(var("x", type("string"), number(10.1))));
//        assertThrows(TypeError.class, () -> checker.visit(var("x", type("string"), bool(true))));
//        assertThrows(TypeError.class, () -> checker.visit(var("x", type("string"), bool(false))));
//    }
//
//    @Test
//    void testVarExplicitTypeWrongStringAssignment() {
//        assertThrows(TypeError.class, () -> checker.visit(var("x", type("number"), string("10"))));
//    }
//
//    @Test
//    void testExplicitTypeWrongBoolAssignment() {
//        assertThrows(TypeError.class, () -> checker.visit(var("x", type("number"), bool(false))));
//        assertThrows(TypeError.class, () -> checker.visit(var("x", type("number"), bool(true))));
//    }
//
//    @Test
//    void testGlobalVarNonExisting() {
//        checker = new TypeChecker(new TypeEnvironment());
//        assertThrows(NotFoundException.class, () -> checker.visit(id("VERSION")));
//    }
//
//    @Test
//    void testNull() {
//        var t = checker.visit(var("x", type("string"), string("")));
//        assertEquals(t, ValueType.String);
//    }
//
//    @Test
//    void testInferTypeFromAnotherVar() {
//        var t1 = checker.visit(var("x", type("string"), string("first")));
//        var t2 = checker.visit(var("y", type("string"), id("x")));
//        assertEquals(t1, ValueType.String);
//        assertEquals(t2, ValueType.String);
//    }
}
