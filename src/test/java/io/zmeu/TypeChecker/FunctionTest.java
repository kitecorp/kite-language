package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.ParserErrors;
import io.zmeu.TypeChecker.Types.Type;
import io.zmeu.TypeChecker.Types.TypeFactory;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeChecker Function")
public class FunctionTest extends CheckerTest {

    @Test
    void testFunInputAndReturn() {
        var actual = eval("""
                fun square(number   x  )  number {
                    return x * x
                }
                // square(2)
                """);
        assertNotNull(actual);
        Type expected = TypeFactory.fromString("(number)->number");
        assertEquals(expected, actual);
    }

    @Test
    void testNoParamTypes() {
        eval(" fun square(x)  number { return x * x } ");
        Assertions.assertTrue(ParserErrors.hadErrors());
    }

    @Test
    void testNoParams() {
        var actual = eval(" fun square()  number { return 2 * 2 } ");
        Type expected = TypeFactory.fromString("()->number");
        assertEquals(expected, actual);
    }

    @Test
    void testVoidThrows() {
        assertThrows(TypeError.class, () -> eval(" fun square()  void { return 2 * 2 } "));
    }

    @Test
    void testVoidDoesntThrow() {
        var actual = eval(" fun square()  void { return; } ");
        Type expected = TypeFactory.fromString("()->void");
        assertEquals(expected, actual);
    }

    @Test
    void testVoidNoReturnDefauly() {
        var actual = eval(" fun square() { return; } ");
        Type expected = TypeFactory.fromString("()->void");
        assertEquals(expected, actual);
    }

    @Test
    void testThrowIfVoidButReturns() {
        assertThrows(TypeError.class, () -> eval(" fun square() { return 2; } "));
    }


    @Test
    void testFunCall() {
        var actual = eval("""
                fun square(number x  )  number {
                    return x * x
                }
                square(2)
                """);
        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testFunCallDecimal() {
        var actual = eval("""
                fun square(number x  )  number {
                    return x * x
                }
                square(2.2)
                """);
        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testFunCallWrongArg() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                fun square(number   x  )  number {
                    return x * x
                }
                square("2")
                """));
    }

    @Test
    void testFunCallWrongNumberOfArg() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                fun square(number  x  )  number {
                    return x * x
                }
                square(2,3)
                """));
    }

    @Test
    void testFunCallWrongNumberOfParam() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                fun square(number x  ,number y  )  number {
                    return x * y
                }
                square(2)
                """));
    }

    @Test
    void testMultiInputsAndReturn() {
        var actual = eval("""
                fun multiply(number  x  ,number y  )  number {
                    return x * y
                }
                multiply(2, 3)
                """);
        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testBuiltIn() {
        var actual = eval(" pow(2, 3) ");
        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testWrongArgType() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                fun multiply(number x  ,number y )  number {
                    return x * y
                }
                multiply(2, "3")
                """));
    }

    @Test
    void testHigherOrderFunction() {
        var actual = eval("""
                var global=10
                fun outer(number x ,number y )  (number)->number {
                    var z = x+y
                
                    fun inner(number p) number {
                        return p+z+global
                    }
                    return inner
                }
                var fn = outer(1,1)
                fn(1)
                """);

        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testClojureFunctionMultipleArgs() {
        var actual = eval("""
                var global=10
                fun outer(number x  , number y  )  (number,number,number)->number {
                    var z = x+y
                
                    fun inner(number p , number q ,number o ) number  {
                        return p+z+global
                    }
                    return inner
                }
                var fn = outer(1,1)
                fn(1,2,3)
                """);

        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void testRecursionFun() {
        var actual = eval("""
                fun recursive(number  x  )  number  {
                    if (x == 1) return x
                    return recursive(x-1)
                }
                recursive(3)
                """);

        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

}
