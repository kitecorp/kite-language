package io.kite.execution.decorators;

import io.kite.analysis.visitors.SyntaxPrinter;
import io.kite.base.RuntimeTest;
import io.kite.execution.Interpreter;
import io.kite.semantics.TypeChecker;
import io.kite.semantics.scope.ScopeResolver;
import io.kite.syntax.ast.KiteCompiler;
import io.kite.tool.theme.PlainTheme;
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
