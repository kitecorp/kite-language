package io.kite.Runtime.Decorators;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parser.Parser;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Interpreter;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.TypeChecker.TypeChecker;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

/**
 * Maybe print the description of the fields
 */
@Log4j2
public class DescriptionTests extends RuntimeTest {
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
    void dependsOnSingleResource() {
        var res = (ResourceValue) eval("""
                schema vm { string name }
                
                resource vm first { }
                
                @description("hello")
                resource vm second {
                
                }
                """);
        log.warn(res);
    }


}
