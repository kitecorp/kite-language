package io.zmeu.Runtime;

import io.zmeu.Base.RuntimeTest;
import io.zmeu.Frontend.Parser.Expressions.VarDeclaration;
import io.zmeu.Frontend.Parse.Literals.Identifier;
import io.zmeu.Frontend.Parse.Literals.NumberLiteral;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import io.zmeu.Frontend.Parser.Statements.ExpressionStatement;
import io.zmeu.Frontend.Parser.Statements.VarStatement;
import io.zmeu.Runtime.Values.FunValue;
import io.zmeu.Runtime.exceptions.VarExistsException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class FunTest extends RuntimeTest {

    @Test
    void funDeclaration() {
        var res = (FunValue) eval("""
                fun myFun(){
                    var x = 1
                }
                """);
        var expected = FunValue.of(
                Identifier.id("myFun"),
                List.of(),
                ExpressionStatement.expressionStatement(BlockExpression.block(VarStatement.varStatement(
                        VarDeclaration.of(Identifier.id("x"), NumberLiteral.of(1))))),
                global
        );
        log.warn((res));
        assertEquals(expected, res);
        Assertions.assertEquals(global.get("myFun"), res);
    }

    @Test
    void funReturn() {
        var res = (FunValue) eval("""
                fun myFun(){
                   var x = 1
                   x
                }
                """);
        var expected = FunValue.of(
                Identifier.id("myFun"),
                List.of(),
                ExpressionStatement.expressionStatement(BlockExpression.block(VarStatement.varStatement(
                                VarDeclaration.of(Identifier.id("x"), NumberLiteral.of(1))
                        ),
                        ExpressionStatement.expressionStatement(Identifier.id("x")))),
                global
        );

        log.warn((res));
        assertEquals(expected, res);
        Assertions.assertEquals(global.get("myFun"), res);
    }

    @Test
    void funEvaluateBlock() {
        var res = eval("""
                fun myFun(number x){
                   x
                }
                myFun(2)
                """);
        log.warn((res));
        assertEquals(2, res);
    }
    @Test
    void funEvaluateBlockWithOuter() {
        var res = eval("""
                var x = "global"
                {
                    fun myFun(){
                      println(x)
                      x
                    }
                    myFun()
                    var x="local"
                    myFun()
                    
                }
                """);
        log.warn((res));
        assertEquals("global", res);
    }

    @Test
    void funBody() {
        var res = eval("""
                fun sqrt(number x){
                   x*x
                }
                sqrt(2)
                """);
        log.warn((res));
        assertEquals(4, res);
    }

    @Test
    void funBodyOverlappingWithParam() {
        Assertions.assertThrows(VarExistsException.class, () -> interpret("""
                fun sqrt(number x){
                   var x = 3
                   x*x
                }
                sqrt(2)
                """));
    }

    @Test
    void funBodyMultiParams() {
        var res = eval("""
                fun sqrt(number x,number y){
                   var z = 1
                   x*y+z
                }
                sqrt(2,3)
                """);
        log.warn((res));
        assertEquals(7, res);
    }

    @Test
    void funClojure() {
        var res = eval("""
                {
                    var a = 100
                    fun calc(number x,number y){
                        var z = x+y
                        fun inner(number b){
                            b+z+a
                        }
                        inner
                    }
                    var fn = calc(10,20)
                    fn(30)
                }
                """);
        log.warn((res));
        assertEquals(160, res);
    }

    @Test
    void returnStatement() {
        var res = eval("""
                fun fib(number n) {
                   if (n <= 1) {
                        return n
                   }
                   return fib(n - 2) + fib(n - 1)
                }
                var x = fib(6)
                println("fib result is: ", x)
                x
                    """);
        log.warn((res));
        assertEquals(8, res);
    }


}
