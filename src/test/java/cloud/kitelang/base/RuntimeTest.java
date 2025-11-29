package cloud.kitelang.base;

import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.semantics.scope.ScopeResolver;
import cloud.kitelang.syntax.ast.KiteCompiler;
import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.tool.theme.PlainTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class RuntimeTest {
    protected Interpreter interpreter;
    protected ScopeResolver scopeResolver;
    protected Program program;
    protected SyntaxPrinter printer;
    protected KiteCompiler compiler;

    @BeforeEach
    void reset() {
        init();
    }

    protected void init() {
        this.compiler = new KiteCompiler();
        this.scopeResolver = new ScopeResolver();
        this.printer = new SyntaxPrinter();
        this.printer.setTheme(new PlainTheme());
        this.interpreter = new Interpreter(new Environment<>("global"));
        this.interpreter.setPrinter(printer);
    }

    @AfterEach
    void cleanup() {
        program = null;
    }

    protected Object eval(String source) {
        program = parse(source);
        scopeResolver.resolve(program);
        return interpreter.visit(program);
    }

    protected Object resolve(String source) {
        scopeResolver.resolve(compiler.parse(source));
        return null;
    }

    protected Program parse(String source) {
        return compiler.parse(source);
    }

}