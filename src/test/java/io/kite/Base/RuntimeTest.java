package io.kite.Base;

import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parser.KiteCompiler;
import io.kite.Frontend.Parser.Program;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Interpreter;
import io.kite.Visitors.SyntaxPrinter;
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