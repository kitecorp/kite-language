package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class LambdaTest extends RuntimeTest {

    @Test
    void funDeclaration() {
        Object res = eval("""
                {
                    fun onClick(number callback){
                        var x = 10
                        var y = 20
                        callback(x+y)
                    }
                    onClick((number data)->data*10)
                }""");

        assertEquals(300, res);
    }

    @Test
    void lambdaAssignToVar() {
        Object res = eval("""
                var f = (number x) -> x*x
                f(2)
                """);

        assertEquals(4, res);
    }

    @Test
    void lambdaInvoke() {
        Object res = eval("""
                ((number x) -> x*x) (2)
                """);

        assertEquals(4, res);
    }

    @Test
    void lambdaInvokeClojure() {
        Object res = eval("""
                {
                var y = 3
                ((number x) ->{ 
                    var z=3 
                    x*y+z
                    }) (2)
                }""");

        assertEquals(9, res);
    }

    @Test
    void lambdaInvokeClojure2() {
        Object res = eval("""
                {
                var y = 3
                ((number x) ->{ 
                    var z=3 
                    var y=4
                    x*y+z
                    }) (2)
                }""");

        assertEquals(11, res);
    }

    @Test
    void lambdaInvokeClojureWithingFunction() {
        Object res = eval("""
                                
                var y = 3
                fun foo(number a) {
                    var z=3
                    (number x) -> {
                        var y=4
                        x*y+z+a
                    }
                }
                var cloj = foo(2)
                cloj(1)
                                
                """);

        assertEquals(9, res);
    }

    @Test
    void lambdaInvokeStaticClojure() {
        Object res = eval("""
                                
                var x = 10
                fun foo(){ x }
                fun bar() {
                    var x=20
                    foo() + x
                    
                }
                bar()
                """);

        assertEquals(30, res);
    }

}
