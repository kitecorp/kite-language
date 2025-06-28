package io.zmeu.Runtime;

import io.zmeu.ErrorSystem;
import io.zmeu.Frontend.Parser.Parser;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
public class ValDeclarationTest extends BaseRuntimeTest {


    @Test
    void valNull() {
        var res = eval("val x");
        Assertions.assertFalse(ErrorSystem.getErrors().isEmpty());
        Assertions.assertEquals(Parser.VAL_NOT_INITIALISED.formatted("x"), ErrorSystem.getErrors().getFirst().getMessage());
    }

    @Test
    void varInt() {
        var res = eval("val x = 2");
        assertEquals(2, res);
        log.info((res));
    }


    @Test
    void varDecimal() {
        var res = (Double) eval("val x = 2.1");
        assertEquals(2.1, res);
        log.info((res));
    }

    @Test
    void varBool() {
        var res = (Boolean) eval("val x = true");
        assertTrue(res);
        log.info((res));
    }


    @Test
    void varExpressionPlus() {
        var res = eval("val x = 2+2");
        assertEquals(4, res);
        log.info((res));
    }

    @Test
    void varExpressionMinus() {
        var res = eval("val x = 2-2");
        assertEquals(0, res);
        log.info((res));
    }

    @Test
    void varExpressionMultiplication() {
        var res = eval("val x = 2*2");
        assertEquals(4, res);
        log.info((res));
    }

    @Test
    void varExpressionDivision() {
        var res = eval("val x = 2/2");
        assertEquals(1, res);
        log.info((res));
    }

    @Test
    void varExpressionBoolean() {
        var res = eval("val x = 2==2");
        var expected = true;
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varExpressionBooleanFalse() {
        var res = eval("val x = 2==1");
        var expected = false;
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varMultiDeclaration() {
        var res = eval("""
                {
                    val y = 0
                    y=1
                }
                """);

        log.info((res));
        assertEquals(1, res);
    }

}
