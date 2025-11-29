package cloud.kitelang.semantics;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.execution.exceptions.DeclarationExistsException;
import cloud.kitelang.execution.exceptions.NotFoundException;
import cloud.kitelang.semantics.types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.expressions.ValDeclaration.val;
import static cloud.kitelang.syntax.literals.BooleanLiteral.bool;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.StringLiteral.string;
import static cloud.kitelang.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Val")
@Disabled
public class ValTest extends CheckerTest {

    @Test
    void testGlobalVarEmptyString() {
        checker.getEnv().init("VERSION", ValueType.String);
        var type = checker.visit(id("VERSION"));
        assertEquals(type, ValueType.String);
    }

    @Test
    void testValInt() {
        var type = checker.visit(val("x", number(10)));
        var accessType = checker.visit(id("x"));
        assertEquals(type, ValueType.Number);
        assertEquals(accessType, ValueType.Number);
    }

    @Test
    void testValDecimalInference() {
        var type = checker.visit(val("x", number(10.5)));
        var access = checker.visit(id("x"));
        assertEquals(ValueType.Number, type);
        assertEquals(ValueType.Number, access);
    }

    @Test
    void testValUseBeforeDeclaration() {
        assertThrows(NotFoundException.class,
                () -> checker.visit(val("y", id("x"))));
    }

    @Test
    void testDuplicateValDeclaration() {
        checker.visit(val("x", number(1)));
        assertThrows(DeclarationExistsException.class,
                () -> checker.visit(val("x", number(2))));
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
    void testVarBool() {
        var type = checker.visit(val("x", bool(true)));
        var access = checker.visit(id("x"));
        assertEquals(ValueType.Boolean, type);
        assertEquals(ValueType.Boolean, access);
    }

    @Test
    void testExplicitTypeWrongBoolAssignment() {
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("number"), bool(false))));
        assertThrows(TypeError.class, () -> checker.visit(val("x", type("number"), bool(true))));
    }

    @Test
    void testGlobalVarNonExisting() {
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

    @Test
    void testInvalidReAssignmentString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val x = "abc"
                x = "cd"
                """));
    }

    @Test
    void testInvalidReAssignmentBoolean() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val x = true
                x = false
                """));
    }

    @Test
    void testInvalidReAssignmentObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val x = null
                x = false
                """));
    }

    @Test
    void testInferTypeFromVal() {
        eval("""
                val a = 42;
                val b = a;
                """);
        var ta = checker.getEnv().lookup("a");
        var tb = checker.getEnv().lookup("b");
        assertEquals(ValueType.Number, ta);
        assertEquals(ValueType.Number, tb);
    }


    @Test
    void testImplicitMatchExplicit() {
        eval("""
                val number a = 42;
                """);
        var ta = checker.getEnv().lookup("a");
        assertEquals(ValueType.Number, ta);
    }

    @Test
    void testImplicitNotMatchExplicit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                val number a = true;
                """));
    }

    /**
     * val x = "foo";
     * val number y = x;  // should TypeError
     */
    @Test
    void testExplicitTypeWrongIdAssignment() {
        checker.visit(val("x", string("foo")));
        assertThrows(TypeError.class,
                () -> checker.visit(val("y", type("number"), id("x"))));
    }
}
