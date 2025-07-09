package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Log4j2
@DisplayName("TypeChecker Assignment")
public class AssignmentTest extends CheckerTest {

    @Test
    void testSimpleAssignment() {
        var actual = eval("""
                var x = 10
                x = 1
                """);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testWrongAssignment() {
        assertThrows(TypeError.class, () -> eval("""
                // init with number type
                var x = 10 
                // try to change to boolean should throw
                x = false 
                """));
    }

    @Test
    void testStringInitWrongTypeInt() {
        assertThrows(TypeError.class, () -> eval("var string x =1"));
    }

    @Test
    void testStringInitWrongTypeObject() {
        assertThrows(TypeError.class, () -> eval("""
                var string x = { "env": "prod "}
                """));
    }

    @Test
    void testStringInitWrongTypeBoolean() {
        assertThrows(TypeError.class, () -> eval("""
                var string x = true
                """));
    }

    @Test
    void testStringInitWrongTypeNull() {
        assertThrows(TypeError.class, () -> eval("""
                var string x = null
                """));
    }
//
//    @Test
//    void testStringInitWrongType() {
//        var actual = parse("""
//                var x Number="test"
//                """);
//        var errors = ErrorSystem.getErrors();
//        log.info(actual);
//        log.info(ErrorSystem.errors());
//        Assertions.assertFalse(errors.isEmpty());
//    }


}
