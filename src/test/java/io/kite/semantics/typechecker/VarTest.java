package io.kite.semantics;

import io.kite.base.CheckerTest;
import io.kite.execution.exceptions.NotFoundException;
import io.kite.semantics.types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.syntax.ast.expressions.VarDeclaration.var;
import static io.kite.syntax.literals.BooleanLiteral.bool;
import static io.kite.syntax.literals.Identifier.id;
import static io.kite.syntax.literals.NumberLiteral.number;
import static io.kite.syntax.literals.StringLiteral.string;
import static io.kite.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Var")
public class VarTest extends CheckerTest {

    @Test
    void testGlobalVarEmptyString() {
        checker.getEnv().init("VERSION", ValueType.String);
        var res = checker.visit(id("VERSION"));
        assertEquals(res, ValueType.String);
    }

    @Test
    void testVarInt() {
        var res = checker.visit(var("x", number(10)));
        var accessType = checker.visit(id("x"));
        assertEquals(res, ValueType.Number);
        assertEquals(accessType, ValueType.Number);
    }

    @Test
    void testVarString() {
        var res = checker.visit(var("x", string("hello")));
        var accessType = checker.visit(id("x"));
        assertEquals(res, ValueType.String);
        assertEquals(accessType, ValueType.String);
    }

    @Test
    void testVarExplicitType() {
        var res = checker.visit(var("x", type("string"), string("hello")));
        var accessType = checker.visit(id("x"));
        assertEquals(res, ValueType.String);
        assertEquals(accessType, ValueType.String);
    }

    @Test
    void testVarExplicitTypeWrongNumberAssignment() {
        assertThrows(TypeError.class, () -> checker.visit(var("x", type("string"), number(10))));
        assertThrows(TypeError.class, () -> checker.visit(var("x", type("string"), number(10.1))));
        assertThrows(TypeError.class, () -> checker.visit(var("x", type("string"), bool(true))));
        assertThrows(TypeError.class, () -> checker.visit(var("x", type("string"), bool(false))));
    }

    @Test
    void testVarExplicitTypeWrongStringAssignment() {
        assertThrows(TypeError.class, () -> checker.visit(var("x", type("number"), string("10"))));
    }

    @Test
    void testExplicitTypeWrongBoolAssignment() {
        assertThrows(TypeError.class, () -> checker.visit(var("x", type("number"), bool(false))));
        assertThrows(TypeError.class, () -> checker.visit(var("x", type("number"), bool(true))));
    }

    @Test
    void testGlobalVarNonExisting() {
        assertThrows(NotFoundException.class, () -> checker.visit(id("VERSION")));
    }

    @Test
    void testNull() {
        var t = checker.visit(var("x", type("string"), string("")));
        assertEquals(t, ValueType.String);
    }

    @Test
    void testInferTypeFromAnotherVar() {
        var t1 = checker.visit(var("x", type("string"), string("first")));
        var t2 = checker.visit(var("y", type("string"), id("x")));
        assertEquals(t1, ValueType.String);
        assertEquals(t2, ValueType.String);
    }


    @Test
    void testInferTypeFromVal() {
        eval("""
                var a = 42;
                var b = a;
                """);
        var ta = checker.getEnv().lookup("a");
        var tb = checker.getEnv().lookup("b");
        assertEquals(ValueType.Number, ta);
        assertEquals(ValueType.Number, tb);
    }

    @Test
    void testImplicitMatchExplicit() {
        eval("""
                var number a = 42;
                """);
        var ta = checker.getEnv().lookup("a");
        assertEquals(ValueType.Number, ta);
    }

    @Test
    void testImplicitNotMatchExplicit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var number a = true;
                """));
    }

    @Test
    @Disabled
    void testInferTypeFromValVar() {
        eval("""
                val a = 42;
                var b = a;
                """);
        var ta = checker.getEnv().lookup("a");
        var tb = checker.getEnv().lookup("b");
        assertEquals(ValueType.Number, ta);
        assertEquals(ValueType.Number, tb);
    }

    @Test
    @Disabled
    void testInferTypeFromVarVal() {
        eval("""
                var a = 42;
                val b = a;
                """);
        var ta = checker.getEnv().lookup("a");
        var tb = checker.getEnv().lookup("b");
        assertEquals(ValueType.Number, ta);
        assertEquals(ValueType.Number, tb);
    }


    @Test
    void testInvalidReAssignmentObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var x = null
                x = false
                """));
    }

    @Test
    void testInvalidReAssignmentNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var x = true
                x = 1
                """));
    }

    @Test
    void testInvalidReAssignmentString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var x = true
                x = "sun"
                """));
    }

    @Test
    void testInvalidReAssignment() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var x = true
                x = null
                """));
    }
}
