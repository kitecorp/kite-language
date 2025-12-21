package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Disabled
public class ValDeclarationTest extends RuntimeTest {


    @Test
    void valNull() {
        var err = assertThrows(RuntimeException.class, () -> eval("val x"));
        assertEquals("val declaration must have an initial value", err.getMessage());
    }

    @Test
    void varInt() {
        var res = eval("val x = 2");
        assertEquals(2, res);
    }


    @Test
    void varDecimal() {
        var res = (Double) eval("val x = 2.1");
        assertEquals(2.1, res);
    }

    @Test
    void varBool() {
        var res = (Boolean) eval("val x = true");
        assertTrue(res);
    }


    @Test
    void varExpressionPlus() {
        var res = eval("val x = 2+2");
        assertEquals(4, res);
    }

    @Test
    void varExpressionMinus() {
        var res = eval("val x = 2-2");
        assertEquals(0, res);
    }

    @Test
    void varExpressionMultiplication() {
        var res = eval("val x = 2*2");
        assertEquals(4, res);
    }

    @Test
    void varExpressionDivision() {
        var res = eval("val x = 2/2");
        assertEquals(1, res);
    }

    @Test
    void varExpressionBoolean() {
        var res = eval("val x = 2==2");
        var expected = true;
        assertEquals(expected, res);
    }

    @Test
    void varExpressionBooleanFalse() {
        var res = eval("val x = 2==1");
        var expected = false;
        assertEquals(expected, res);
    }

    @Test
    void varMultiDeclaration() {
        var res = eval("""
                {
                    val y = 0
                    y=1
                }
                """);

        assertEquals(1, res);
    }

}
