package io.kite.Runtime.Decorators;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parser.Parser;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Interpreter;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.TypeError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Log4j2
public class MinMaxLengthTests extends RuntimeTest {
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
    void outputMaxLength() {
        eval("""
                @maxLength(10)
                output string something = "hello"
                """);
    }

    @Test
    void outputMaxLengthGreaterThan() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @maxLength(10)
                output string something = "hello world!!!!!!!!!!!"
                """));
    }

    @Test
    void outputMaxLengthWrongInit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxLength(10)
                output string something = 10
                """));
    }

    @Test
    void outputMaxLengthEmpty() {
        eval("""
                @maxLength(0)
                output string something = ""
                """);
    }

    @Test
    void outputMaxLengthNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @maxLength(0)
                output string something = "a"
                """));
    }

    @Test
    void outputMinLength() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @minLength(10)
                output string something = "hello"
                """));
    }

    @Test
    void outputMinLengthGreaterThan() {
        eval("""
                @minLength(10)
                output string something = "hello world!!!!!!!!!!!"
                """);
    }

    @Test
    void outputMinLengthWrongInit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minLength(10)
                output string something = 10
                """));
    }

    @Test
    void outputMinLengthEmpty() {
        eval("""
                @minLength(0)
                output string something = ""
                """);
    }

    @Test
    void outputMinLengthNegative() {
        eval("""
                @minLength(0)
                output string something = "a"
                """);
    }

    @Test
    void outputSensitive() {
        eval("""
                @sensitive
                output string something = "a"
                """);
    }

    @Test
    void outputCount() {
        eval("""
                schema vm { string name }
                
                @count(2)
                resource vm main {
                   
                }
                """);
    }

}
