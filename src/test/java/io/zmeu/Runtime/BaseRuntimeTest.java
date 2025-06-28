package io.zmeu.Runtime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
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
    protected ObjectMapper gson;
    protected Resolver resolver;

    @BeforeEach
    void reset() {
        this.global = new Environment();
        this.interpreter = new Interpreter(global);
        this.parser = new Parser();
        this.tokenizer = new Tokenizer();
        this.resolver = new Resolver(interpreter);
        gson = JsonMapper.builder() // or different mapper for other format
                .addModule(new ParameterNamesModule())
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .build();
        gson.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        gson.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

        gson.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        gson.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        ErrorSystem.clear();
    }

    @AfterEach
    void cleanup() {
        ErrorSystem.clear();
    }

    protected String toJson(Object o) {
        try {
            return gson.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected Object eval(String source) {
        Program program = parser.produceAST(tokenizer.tokenize(source));
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