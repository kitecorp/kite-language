package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ObjectDeclarationTest extends RuntimeTest {


    @Test
    void varNull() {
        var res = eval("var x={}");
        assertNotNull(res);
        Assertions.assertTrue(global.hasVar("x"));
        var o = (Map) global.get("x");
        assertTrue(o.isEmpty());
        log.info((res));
    }

    @Test
    void varInt() {
        var res = eval("var x = { size: 2 }");
        var o = (Map) global.get("x");
        assertFalse(o.isEmpty());
        assertEquals(2, o.get("size"));
        log.info((res));
    }


    @Test
    void varDecimal() {
        var res = eval("var x = { size: 2.1 }");
        var o = (Map) global.get("x");
        assertEquals(2.1, o.get("size"));
        log.info((res));
    }

    @Test
    void varBool() {
        var res = eval("var x = { size: true }");
        var o = (Map) global.get("x");
        assertEquals(true, o.get("size"));
        log.info((res));
    }


    @Test
    void varExpressionPlus() {
        var res = eval("var x = { size: 2  +  2 }");
        var o = (Map) global.get("x");
        assertEquals(4, o.get("size"));
        log.info((res));
    }

    @Test
    void varExpressionMinus() {
        var res = eval("var x = { size: 2  -  2 }");
        var o = (Map) global.get("x");
        assertEquals(0, o.get("size"));
        log.info((res));
    }

    @Test
    void varExpressionMultiplication() {
        var res = eval("var x = { size: 2  *  2 }");
        var o = (Map) global.get("x");
        assertEquals(4, o.get("size"));
        log.info((res));
    }

    @Test
    void varExpressionDivision() {
        var res = eval("var x = { size: 2  /  2 }");
        var o = (Map) global.get("x");
        assertEquals(1, o.get("size"));
        log.info((res));
    }

    @Test
    void varExpressionBoolean() {
        var res = eval("var x = { size: 2  ==  2 }");
        var o = (Map) global.get("x");
        assertEquals(true, o.get("size"));
        log.info((res));
    }

    @Test
    void varExpressionBooleanFalse() {
        var res = eval("var x = { size: 2  ==  1 }");
        var o = (Map) global.get("x");
        assertEquals(false, o.get("size"));
        log.info((res));
    }

    @Test
    void varMultiDeclaration() {
        var res = eval("""
                var x = { 
                    size: 2
                    color: "white"
                }
                """);
        var o = (Map) global.get("x");
        assertEquals(2, o.get("size"));
        assertEquals("white", o.get("color"));
        log.info((res));
    }

    @Test
    void varPropertyAccess() {
        var res = eval("""
                var x = { 
                    size: 2
                    color: "white"
                }
                var y = x.color
                """);
        var y = (String) global.get("y");
        assertEquals("white", y);
        log.info(res);
    }

    @Test
    void varPropertyAccessNested() {
        var res = eval("""
                var x = { 
                    size: 2
                    color: {
                      name: "white"
                    }
                }
                var y = x.color.name
                """);
        var y = (String) global.get("y");
        assertEquals("white", y);
        log.info(res);
    }

    @Test
    void varPropertyAccessKeyString() {
        var res = eval("""
                var x = { 
                    size: 2
                    color: "white"
                }
                var y = x["color"]
                """);
        var y = (String) global.get("y");
        assertEquals("white", y);
        log.info(res);
    }


    @Test
    void varPropertyAccessNestedKeyString() {
        var res = eval("""
                var x = { 
                    size: 2
                    color: {
                      name: "white"
                    }
                }
                var y = x.color["name"]
                """);
        var y = (String) global.get("y");
        assertEquals("white", y);
        log.info(res);
    }

    @Test
    void varMultiDeclarationStringKey() {
        var res = eval("""
                var x = { 
                    size: 2
                    "color": "white"
                }
                """);
        var o = (Map) global.get("x");
        assertEquals(2, o.get("size"));
        assertEquals("white", o.get("color"));
        log.info((res));
    }

    @Test
    void varMultiDeclarationSingleQuote() {
        var res = eval("""
                var x = { 
                    size: 2
                    'color-name': "white"
                }
                """);
        var o = (Map) global.get("x");
        assertEquals(2, o.get("size"));
        assertEquals("white", o.get("color-name"));
        log.info((res));
    }

    @Test
    void varMultiDeclarationObject() {
        var res = eval("""
                var x = { 
                    size: 2
                    env: {
                        color: "white"
                    }
                }
                """);
        var o = (Map) global.get("x");
        assertEquals(2, o.get("size"));
        var env = (Map<String, Object>) o.get("env");
        assertEquals("white", env.get("color"));
        log.info((res));
    }

}
