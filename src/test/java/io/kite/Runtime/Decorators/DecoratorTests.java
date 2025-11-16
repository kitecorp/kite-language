package io.kite.Runtime.Decorators;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parser.KiteCompiler;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Interpreter;
import io.kite.TypeChecker.TypeChecker;
import io.kite.Visitors.PlainTheme;
import lombok.extern.log4j.Log4j2;

/**
 * Maybe print the description of the fields
 */
@Log4j2
public class DecoratorTests extends RuntimeTest {
    private TypeChecker typeChecker;

    @Override
    protected void init() {
        this.compiler = new KiteCompiler();
        this.typeChecker = new TypeChecker();
        this.scopeResolver = new ScopeResolver();
        this.interpreter = new Interpreter(new Environment<>("global"));
        this.interpreter.getPrinter().setTheme(new PlainTheme());
    }


    protected Object eval(String source) {
        program = parse(source);
        scopeResolver.resolve(program);
        typeChecker.visit(program);
        return interpreter.visit(program);
    }

}
