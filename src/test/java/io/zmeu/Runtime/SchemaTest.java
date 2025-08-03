package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Runtime.Values.FunValue;
import io.kite.Runtime.Values.SchemaValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.kite.Frontend.Parse.Literals.ParameterIdentifier.param;
import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class SchemaTest extends RuntimeTest {

    @Test
    void declare() {
        var res = eval("""
                schema Vm {
                
                }
                """);
        log.warn(res);
        var actual = (SchemaValue) global.get("Vm");

        assertEquals("Vm", actual.getType());
    }

    @Test
    @Disabled("functions not implemented yet")
    void declareWithFunction() {
        var res = eval("""
                schema Vm {
                    fun test(){
                
                    }
                }
                """);
        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        Assertions.assertEquals(FunValue.of("test", actual.getEnvironment()), actual.getEnvironment().lookup("test"));
    }

    @Test
    void declareWithVariable() {
        var res = eval("""
                schema Vm {
                    var int x
                }
                """);
        log.warn(res);
        var actual = (SchemaValue) global.get("Vm");

        assertTrue(actual.has("x"));
    }

    @Test
    void declareMultipleProperties() {
        var res = eval("""
                schema Vm {
                   var int x
                   var int y // init not mandatory in schema
                }
                """);
        log.warn(res);
        var actual = (SchemaValue) global.get("Vm");

        assertTrue(actual.has("x"));
        assertTrue(actual.has("y"));
        assertNull(actual.get("x"));
        assertNull(actual.get("y"));
    }

    @Test
    void declareWithVariableInit() {
        var res = eval("""
                schema Vm {
                   var int x = 20.2
                }
                """);
        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        Assertions.assertEquals(20.2, actual.get("x"));
    }

    @Test
    void declareWithVariableInitString() {
        var res = eval("""
                schema Vm {
                   var string x = "hello"
                }
                """);
        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        Assertions.assertEquals("hello", actual.get("x"));
    }

    @Test
    void declareWithVarValInit() {
        var res = eval("""
                schema Vm {
                   var int x = 20.2
                   var int y = 20.2 // init can be a default value schema
                }
                """);
        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        Assertions.assertEquals(20.2, actual.get("x"));
        Assertions.assertEquals(20.2, actual.get("y"));
    }

    @Test
    void declareWithVariableValInitString() {
        var res = eval("""
                schema Vm {
                    var string x = "hello"
                    var String y = "hello"
                }
                """);
        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        Assertions.assertEquals("hello", actual.get("x"));
        Assertions.assertEquals("hello", actual.get("x"));
    }

    @Test
    @Disabled("functions not implemented yet")
    void initDeclaration() {
        var res = eval("""
                schema Vm {
                    init(){
                
                    }
                }
                """);

        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        Assertions.assertEquals(FunValue.of("init", actual.getEnvironment()), actual.getEnvironment().lookup("init"));
    }

    @Test
    @Disabled("functions not implemented yet")
    void initDeclarationWithParams() {
        var res = eval("""
                schema Vm {
                    init(object x){
                
                    }
                }
                """);

        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        Assertions.assertEquals(FunValue.of("init", List.of(param("x", "object")), actual.getEnvironment()), actual.getEnvironment().lookup("init"));
    }

    @Test
    void initDeclarationWithParamsAssignment() {
        var res = eval("""
                schema Vm {
                    var number x = 1;
                }
                """);

        log.warn(res);
        var actual = (SchemaValue) global.get("Vm");

        assertEquals(res, actual);
        Assertions.assertEquals(1, actual.getEnvironment().get("x"));
    }

    @Test
    @Disabled
    void initDeclarationWithParamsValVarAssignment() {
        var res = eval("""
                schema Vm {
                    var number x = 1;
                    val number y = 1;
                }
                """);

        log.warn(res);
        var actual = (SchemaValue) global.get("Vm");

        assertEquals(res, actual);
        Assertions.assertEquals(1, actual.getEnvironment().get("x"));
        Assertions.assertEquals(1, actual.getEnvironment().get("y"));
    }

    @Test
    void initDeclarationWithPathType() {
        var res = eval("""
                schema Vm {
                   var Number x = 1;
                }
                """);

        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        assertNotNull(res);
        assertEquals(res, actual);
        Assertions.assertEquals(1, actual.getEnvironment().get("x"));
    }

    @Test
    void initDeclarationWithPathTypeVarVal() {
        var res = eval("""
                schema Vm {
                    var Number x = 1;
                    var Number y = 1;
                }
                """);

        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        assertNotNull(res);
        assertEquals(res, actual);
        Assertions.assertEquals(1, actual.get("x"));
        Assertions.assertEquals(1, actual.get("y"));
    }

    @Test
    void initDeclarationWithWrongVar() {
        var res = eval("""
                schema Vm {
                   var Number x  = "test";
                }
                """);

        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        assertNotNull(res);
        assertEquals(res, actual);
    }

    @Test
    void initDeclarationWithWrontInit() {
        var res = eval("""
                schema Vm {
                    var Number y  = "test";
                }
                """);

        log.warn((res));
        var actual = (SchemaValue) global.get("Vm");

        assertNotNull(res);
        assertEquals(res, actual);
    }


}
