package io.kite.Base;

import io.kite.Frontend.Parser.ParserErrors;
import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Lexical.Resolver;
import io.kite.Frontend.Parser.Parser;
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
    protected Environment<Object> global;
    protected Resolver resolver;
    protected Program program;
    protected SyntaxPrinter printer = new SyntaxPrinter();

    @BeforeEach
    void reset() {
        init();
        ParserErrors.clear();
    }

    protected void init() {
        this.global = new Environment<>();
        this.global.setName("global");
        this.interpreter = new Interpreter(global);
        this.parser = new Parser();
        this.tokenizer = new Tokenizer();
        this.resolver = new Resolver(interpreter);
    }

    @AfterEach
    void cleanup() {
        ParserErrors.clear();
        program = null;
    }

    protected Object eval(String source) {
        program = src(source);
        resolver.resolve(program);
        return interpreter.visit(program);
    }

    protected Object interpret(String source) {
        return interpreter.visit(parser.produceAST(tokenizer.tokenize(source)));
    }

    protected Object resolve(String source) {
        resolver.resolve(parser.produceAST(tokenizer.tokenize(source)));
        return null;
    }

    protected Object parse(String source) {
        return parser.produceAST(tokenizer.tokenize(source));
    }

    protected Program src(String source) {
        return parser.produceAST(tokenizer.tokenize(source));
    }

}