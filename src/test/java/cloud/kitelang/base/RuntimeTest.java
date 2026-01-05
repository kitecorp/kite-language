package cloud.kitelang.base;

import cloud.kitelang.analysis.ImportResolver;
import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.semantics.scope.ScopeResolver;
import cloud.kitelang.syntax.ast.KiteCompiler;
import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.tool.theme.PlainTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

public class RuntimeTest {
    /**
     * Base path for resolving relative import paths in tests.
     * Points to src/test/resources so tests can use clean paths like "providers/networking".
     */
    protected static final Path TEST_RESOURCES_PATH = Path.of("src/test/resources");

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

        // Set base path for import resolution so tests can use relative paths
        ImportResolver.setBasePath(TEST_RESOURCES_PATH);
    }

    @AfterEach
    void cleanup() {
        program = null;
        // Clear base path after each test
        ImportResolver.setBasePath(null);
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