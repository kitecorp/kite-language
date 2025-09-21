package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parser.Parser;
import io.kite.Runtime.Environment.Environment;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.TypeError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Log4j2
public class DecoratorTests extends RuntimeTest {
    private TypeChecker typeChecker;

    @Override
    protected void init() {
        this.global = new Environment<>();
        this.global.setName("global");
        this.parser = new Parser();
        this.tokenizer = new Tokenizer();
        this.typeChecker = new TypeChecker();
        this.scopeResolver = new ScopeResolver();
        this.interpreter = new Interpreter(global);
    }


    protected Object eval(String source) {
        program = src(source);
        scopeResolver.resolve(program);
        typeChecker.visit(program);
        return interpreter.visit(program);
    }

    @Test
    void outputMinValue() {
        eval("""
                @minValue(10)
                output number something = 10
                """);
    }

    @Test
    void outputMinValueGreaterThan() {
        eval("""
                @minValue(10)
                output number something = 11
                """);
    }

    @Test
    void outputMinValueLessThan() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @minValue(10)
                output number something = 9
                """));
    }

    @Test
    void outputMinValueWrongInit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue(10)
                output number something = "hello"
                """));
    }

    @Test
    void outputMinValueNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @minValue(10)
                output number something = -9
                """));
    }
    @Test
    void outputMaxValue() {
        eval("""
                @maxValue(10)
                output number something = 10
                """);
    }

    @Test
    void outputMaxValueGreaterThan() {
        eval("""
                @maxValue(10)
                output number something = 11
                """);
    }

    @Test
    void outputMaxValueLessThan() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @maxValue(10)
                output number something = 9
                """));
    }

    @Test
    void outputMaxValueWrongInit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue(10)
                output number something = "hello"
                """));
    }

    @Test
    void outputMaxValueNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @maxValue(10)
                output number something = -9
                """));
    }


}
