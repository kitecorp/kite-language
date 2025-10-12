package io.kite.Base;

import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parser.Parser;
import io.kite.Frontend.Parser.ParserErrors;
import io.kite.Frontend.Parser.Program;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Interpreter;
import io.kite.Visitors.SyntaxPrinter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class RuntimeTest {
    protected Interpreter interpreter;
    protected Parser parser;
    protected Tokenizer tokenizer;
    protected ScopeResolver scopeResolver;
    protected Program program;
    protected SyntaxPrinter printer = new SyntaxPrinter();
    protected Environment<Object> global;

    @BeforeEach
    void reset() {
        init();
        ParserErrors.clear();
    }

    protected void init() {
        this.global = new Environment<>("global");
        this.tokenizer = new Tokenizer();
        this.parser = new Parser();
        this.scopeResolver = new ScopeResolver();
        this.interpreter = new Interpreter(global);
    }

    @AfterEach
    void cleanup() {
        ParserErrors.clear();
        program = null;
    }

    protected Object eval(String source) {
        program = parse(source);
        scopeResolver.resolve(program);
        return interpreter.visit(program);
    }

    protected Object resolve(String source) {
        scopeResolver.resolve(parser.produceAST(tokenizer.tokenize(source)));
        return null;
    }

    protected Program parse(String source) {
        return parser.produceAST(tokenizer.tokenize(source));
    }

}