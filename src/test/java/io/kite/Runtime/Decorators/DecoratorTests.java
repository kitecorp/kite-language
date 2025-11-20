package io.kite.Runtime.Decorators;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parser.KiteCompiler;
import io.kite.Runtime.Interpreter;
import io.kite.TypeChecker.TypeChecker;
import io.kite.Visitors.PlainTheme;
import io.kite.Visitors.SyntaxPrinter;
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
        this.printer = new SyntaxPrinter(new PlainTheme());
        this.typeChecker = new TypeChecker(printer);
        this.scopeResolver = new ScopeResolver();
        this.interpreter = new Interpreter(printer);
        this.interpreter.setPrinter(printer);
    }


    protected Object eval(String source) {
        program = parse(source);
        scopeResolver.resolve(program);
        typeChecker.visit(program);
        return interpreter.visit(program);
    }

}
