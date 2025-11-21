package io.kite.runtime.decorators;

import io.kite.base.RuntimeTest;
import io.kite.frontend.lexical.ScopeResolver;
import io.kite.frontend.parser.KiteCompiler;
import io.kite.runtime.Interpreter;
import io.kite.typechecker.TypeChecker;
import io.kite.visitors.PlainTheme;
import io.kite.visitors.SyntaxPrinter;
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
