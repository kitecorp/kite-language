package cloud.kitelang.execution.decorators;

import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.scope.ScopeResolver;
import cloud.kitelang.syntax.ast.KiteCompiler;
import cloud.kitelang.tool.theme.PlainTheme;
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
