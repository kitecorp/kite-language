package io.zmeu.Runtime;

import io.zmeu.ErrorSystem;
import io.zmeu.Frontend.Lexer.Tokenizer;
import io.zmeu.Frontend.Lexical.Resolver;
import io.zmeu.Frontend.Parser.Parser;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Runtime.Environment.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseRuntimeTest {
    protected Interpreter interpreter;
    protected Parser parser;
    protected Tokenizer tokenizer;
    protected Environment global;
    protected Resolver resolver;
    protected Program program;

    @BeforeEach
    void reset() {
        this.global = new Environment();
        this.interpreter = new Interpreter(global);
        this.parser = new Parser();
        this.tokenizer = new Tokenizer();
        this.resolver = new Resolver(interpreter);
        ErrorSystem.clear();
    }

    @AfterEach
    void cleanup() {
        ErrorSystem.clear();
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