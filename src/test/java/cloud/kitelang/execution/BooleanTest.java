package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@DisplayName("TypeChecker Boolean")
class BooleanTest extends RuntimeTest {

    @Test
    void testTrue() {
        var t1 = (boolean) eval("true");
        assertTrue(t1);
    }

    @Test
    void testFalse() {
        var t1 = (boolean) eval("false");
        assertFalse(t1);
    }

    @Test
    void testNull() {
        var t1 = eval("null");
        assertNull(t1);
    }

    @Test
    void testEq() {
        var t1 = (boolean) eval("1==1");
        Assertions.assertTrue(t1);
    }

    @Test
    void testStringWithStringEq() {
        var res = (boolean) eval("""
                "hello" == "hello"
                """);
        assertTrue(res);
    }

    @Test
    void testStringWithStringEqFalse() {
        var res = (boolean) eval("""
                "hello" == "Hello"
                """);
        assertFalse(res);
    }

    @Test
    void testStringWithStringNotEq() {
        var res = (boolean) eval("""
                "hello" != "world"
                """);
        assertTrue(res);
    }

    @Test
    void testStringWithStringNotEqFalse() {
        var res = (boolean) eval("""
                "hello" != "hello"
                """);
        assertFalse(res);
    }

    @Test
    void testStringLessFalseSame() {
        var res = (boolean) eval("""
                "ab" < "ab"
                """);
        assertFalse(res);
    }

    @Test
    void testStringLessTrueDifferent() {
        var res = (boolean) eval("""
                "ab" < "ac"
                """);
        assertTrue(res);
    }

    @Test
    void testStringLessEqTrueDifferent() {
        var res = (boolean) eval("""
                "ab" <= "ab"
                """);
        assertTrue(res);
    }

    @Test
    void testStringLessEqTrueSame() {
        var res = (boolean) eval("""
                "ab" <= "ac"
                """);
        assertTrue(res);
    }

    @Test
    void testStringLessEqFalseSame() {
        var res = (boolean) eval("""
                "ad" <= "ac"
                """);
        assertFalse(res);
    }

    @Test
    void testStringGreaderFalseSame() {
        var res = (boolean) eval("""
                "ab" > "ab"
                """);
        assertFalse(res);
    }

    @Test
    void testStringGreaderTrueDifferent() {
        var res = (boolean) eval("""
                "ac" > "ab"
                """);
        assertTrue(res);
    }

    @Test
    void testStringGreaterEqTrueDifferent() {
        var res = (boolean) eval("""
                "ab" >= "ab"
                """);
        assertTrue(res);
    }

    @Test
    void testStringGreaderEqTrueSame() {
        var res = (boolean) eval("""
                "ac" >= "ab"
                """);
        assertTrue(res);
    }

    @Test
    void testStringGreaterEqFalseSame() {
        var res = (boolean) eval("""
                "ac" >= "ad"
                """);
        assertFalse(res);
    }

    @Test
    void testTrueWithTrueEq() {
        var res = (boolean) eval("""
                true == true
                """);
        assertTrue(res);
    }

    @Test
    void testTrueWithFalseEq() {
        var res = (boolean) eval("""
                true == false
                """);
        assertFalse(res);
    }

    @Test
    void testFalseWithFalseEq() {
        var res = (boolean) eval("""
                false == false
                """);
        assertTrue(res);
    }

    @Test
    void testFalseWithTrueEq() {
        var res = (boolean) eval("""
                false == true
                """);
        assertFalse(res);
    }

    @Test
    void testFalseWithTrueLess() {
        var res = (boolean) eval("""
                false < true
                """);
        assertTrue(res);
    }

    @Test
    void testFalseWithTrueLessEq() {
        var res = (boolean) eval("""
                false <= true
                """);
        assertTrue(res);
    }

    @Test
    void testFalseWithTrueGreater() {
        var res = (boolean) eval("""
                false > true
                """);
        assertFalse(res);
    }

    @Test
    void testFalseWithTrueGreaterEq() {
        var res = (boolean) eval("""
                false >= true
                """);
        assertFalse(res);
    }

    @Test
    void testNumberWithTrueGreaterEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                1 >= true
                """));
    }

    @Test
    void testNumberWithFalseGreaterEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                1 >= true
                """));
    }

    @Test
    void testNumberWithBoolGreaterEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                1 > true
                """));
    }

    @Test
    void testNumberWithBoolLessEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                1 <= true
                """));
    }

    @Test
    void testNumberWithBoolLess() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                1 < true
                """));
    }

    @Test
    void testNumberWithNumberLess() {
        var actual = (boolean) eval("""
                1 < 2
                """);
        assertTrue(actual);
    }

    @Test
    void testNumberWithNumberLessEq() {
        var actual = (boolean) eval("""
                1 <= 2
                """);
        assertTrue(actual);
    }

    @Test
    void testNumberWithNumberGreaterEq() {
        var actual = (boolean) eval("""
                1 >= 2
                """);
        assertFalse(actual);
    }

    @Test
    void testNumberWithNumberGreater() {
        var actual = (boolean) eval("""
                1 > 2
                """);
        assertFalse(actual);
    }

    @Test
    void testNumberWithNumberEq() {
        var actual = (boolean) eval("""
                1 == 2
                """);
        assertFalse(actual);
    }

    @Test
    void testNumberWithNumberNotEq() {
        var actual = (boolean) eval("""
                1 != 2
                """);
        assertTrue(actual);
    }

    @Test
    void testStringWithNumberLess() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                "hello" < 1
                """));
    }

    @Test
    void testStringWithNumberLessEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                "hello" <= 1
                """));
    }

    @Test
    void testStringWithNumberGreaterEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                "hello" >= 1
                """));
    }

    @Test
    void testStringWithNumberEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                "hello" == 1
                """));
    }

    @Test
    void testStringWithNumberNotEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                "hello" != 1
                """));
    }

    @Test
    void testBoolWithNumberNotEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                true != 1
                """));
    }

    @Test
    void testBoolWithNumberEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                true == 1
                """));
    }

    @Test
    void testBoolWithNumberGreaterEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                true >= 1
                """));
    }

    @Test
    void testBoolWithNumberGreater() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                true > 1
                """));
    }

    @Test
    void testBoolWithNumberLess() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                true < 1
                """));
    }

    @Test
    void testBoolWithNumberLessEq() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                true <= 1
                """));
    }


    @Test
    @Disabled
    void testObjectEqual() {
        var res = (boolean) eval("""
                val x = { "env": "prod" }
                val y = { "env": "prod" }
                x == y
                """);
        assertTrue(res);
    }

    @Test
    @Disabled
    void testObjectNotEqual() {
        var res = (boolean) eval("""
                val x = { "env": "prod" }
                val y = { "env": "prod" }
                x != y
                """);
        assertFalse(res);
    }

    @Test
    @Disabled
    void testObjectNotEqualTrue() {
        var res = (boolean) eval("""
                val x = { "env": "dev" }
                val y = { "env": "prod" }
                x != y
                """);
        assertTrue(res);
    }

    @Test
    void testVarObjectNotEqual() {
        var res = (boolean) eval("""
                var x = { "env": "prod" }
                var y = { "env": "prod" }
                x != y
                """);
        Assertions.assertFalse(res);
    }

    @Test
    void testVarObjectEqual() {
        var res = (boolean) eval("""
                var x = { "env": "prod" }
                var y = { "env": "prod" }
                x == y
                """);
        Assertions.assertTrue(res);
    }

    @Test
    void testVarObjectEqualMoreProperties() {
        var res = (boolean) eval("""
                var x = { "env": "prod" }
                var y = { "env": "prod", size: 1 }
                x == y
                """);
        Assertions.assertFalse(res);
    }

    @Test
    void testVarObjectEqualOrder() {
        var res = (boolean) eval("""
                var x = { size: 1, "env": "prod" }
                var y = { "env": "prod", size: 1 }
                x == y
                """);
        Assertions.assertTrue(res);
    }

    @Test
    void testVarObjectEqualDifferentType() {
        var res = (boolean) eval("""
                var x = { size: true, "env": "prod" }
                var y = { "env": "prod", size: 1 }
                x == y
                """);
        Assertions.assertFalse(res);
    }

    @Test
    void testVarObjectEqualNested() {
        var res = (boolean) eval("""
                var x = { 
                    env: "prod", 
                    size: {
                        UE: "xl"
                    } 
                }
                var y = { 
                    env: "prod", 
                    size: {
                        UE: "xl"
                    } 
                }
                x == y
                """);
        Assertions.assertTrue(res);
    }

    @Test
    void testVarObjectEqualNestedDifferentKey() {
        var res = (boolean) eval("""
                var x = { 
                    env: "prod", 
                    size: {
                        US: "xl"
                    } 
                }
                var y = { 
                    env: "prod", 
                    size: {
                        UE: "xl"
                    } 
                }
                x == y
                """);
        Assertions.assertFalse(res);
    }

    @Test
    void testVarObjectEqualNestedDifferentValue() {
        var res = (boolean) eval("""
                var x = { 
                    env: "prod", 
                    size: {
                        UE: "m"
                    } 
                }
                var y = { 
                    env: "prod", 
                    size: {
                        UE: "xl"
                    } 
                }
                x == y
                """);
        Assertions.assertFalse(res);
    }


}