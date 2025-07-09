package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.Runtime.exceptions.NotFoundException;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parse.Literals.BooleanLiteral.bool;
import static io.zmeu.Frontend.Parse.Literals.Identifier.id;
import static io.zmeu.Frontend.Parse.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parse.Literals.StringLiteral.string;
import static io.zmeu.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.zmeu.Frontend.Parser.Expressions.ValDeclaration.val;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Val")
public class ValTest extends CheckerTest {

    @Test
    void testGlobalVarEmptyString() {
        checker = new TypeChecker(new TypeEnvironment());
        checker.getEnv().init("VERSION", ValueType.String);
        var type = checker.visit(id("VERSION"));
        assertEquals(type, ValueType.String);
    }

    @Test
    void testVarInt() {
        var type = checker.visit(val("x", number(10)));
        var accessType = checker.visit(id("x"));
        assertEquals(type, ValueType.Number);
        assertEquals(accessType, ValueType.Number);
    }

    @Test
    void testVarString() {
        var type = checker.visit(val("x", string("hello")));
        var accessType = checker.visit(id("x"));
        assertEquals(type, ValueType.String);
        assertEquals(accessType, ValueType.String);
    }

    @Test
    void testVarExplicitType() {
        var type = checker.visit(val("x", type("string"), string("hello")));
        var accessType = checker.visit(id("x"));
        assertEquals(type, ValueType.String);
        assertEquals(accessType, ValueType.String);
    }

    @Test
    void testVarExplicitTypeWrongNumberAssignment() {
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("string"), number(10))));
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("string"), number(10.1))));
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("string"), bool(true))));
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("string"), bool(false))));
    }

    @Test
    void testVarExplicitTypeWrongStringAssignment() {
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("number"), string("10"))));
    }

    @Test
    void testExplicitTypeWrongBoolAssignment() {
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("number"), bool(false))));
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("number"), bool(true))));
    }

    @Test
    void testGlobalVarNonExisting() {
        checker = new TypeChecker(new TypeEnvironment());
        assertThrows(NotFoundException.class, () -> checker.visit(id("VERSION")));
    }

    @Test
    void testNull() {
        var t = checker.visit(val("x", type("string"), string("")));
        assertEquals(t, ValueType.String);
    }

    @Test
    void testInferTypeFromAnotherVar() {
        var t1 = checker.visit(val("x", type("string"), string("first")));
        var t2 = checker.visit(val("y", type("string"), id("x")));
        assertEquals(t1, ValueType.String);
        assertEquals(t2, ValueType.String);
    }

    @Test
    void testInvalidAssignment() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val x = 10
                x = "hello"
                """));
    }

    @Test
    void testInvalidReAssignment() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val x = 10
                x = 2
                """));
    }
}
