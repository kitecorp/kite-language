package io.zmeu.TypeChecker;

import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("TypeChecker Lambdas")
public class LambdaTest extends BaseChecker {

    @Test
    void lambdaWithReturn() {
        var actual = eval("""
                fun onClick((number)->number callback) number {
                    return callback(2)
                }
                onClick((number arg ) number -> arg*2)
                """);
        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void lambdaWithReturnInferred() {
        var actual = eval("""
                fun onClick((number)->number callback) number {
                    return callback(2)
                }
                onClick((number arg ) -> arg*2)
                """);
        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void lambdaImmediatelyInvoke() {
        var actual = eval("((number x ) number -> x+2)(3)");
        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void varLambdaInvokeByVariable() {
        var actual = eval("""
                var varLambda = (number x ) number -> x+2
                varLambda(3)
                """);
        assertNotNull(actual);
        assertEquals(ValueType.Number, actual);
    }


}
