package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parser.Parser;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Inputs.ChainResolver;
import io.kite.Runtime.exceptions.MissingInputException;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.TypeError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Log4j2
public class InputTest extends RuntimeTest {

    private InputStream sysInBackup = System.in;
    private ChainResolver chainResolver;
    private TypeChecker typeChecker;

    @AfterEach
    void cleanup() {
        System.setIn(sysInBackup);
    }

    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    private void setInput(Integer input) {
        setInput(input.toString());
    }

    private void setInput(Boolean input) {
        setInput(input.toString());
    }

    private void setInput(Double input) {
        setInput(input.toString());
    }

    private void setInput(Float input) {
        setInput(input.toString());
    }

    @Override
    protected void init() {
        this.global = new Environment<>();
        this.global.setName("global");
        this.parser = new Parser();
        this.tokenizer = new Tokenizer();
        this.typeChecker = new TypeChecker();
        Environment<Object> inputs = new Environment<>(global);
        inputs.setName("inputs");
        this.chainResolver = new ChainResolver(global);
        this.scopeResolver = new ScopeResolver();
        this.interpreter = new Interpreter(global);
    }

    protected Object eval(String source) {
        program = src(source);
        scopeResolver.resolve(program);
        chainResolver.visit(program);
        typeChecker.visit(program);
        return interpreter.visit(program);
    }

    /**************
     * HAPPY CASES *
     * *************/
    @Test
    void inputString() {
        setInput("hello");
        var res = eval("input string region");
        assertEquals("hello", res);
    }

    @Test
    void inputNumber() {
        setInput(10);
        var res = eval("input number region");
        assertEquals(10, res);
    }

    @Test
    void inputDecimal() {
        setInput(10.2);
        var res = eval("input number region");
        assertEquals(10.2, res);
    }

    @Test
    void inputDecimalHalf() {
        setInput(0.2);
        var res = eval("input number region");
        assertEquals(0.2, res);
    }

    @Test
    void inputBoolean() {
        setInput(true);
        var res = eval("input boolean region");
        assertEquals(true, res);
    }

    @Test
    void inputObject() {
        setInput("{ env : 'dev', region : 'us-east-1' }");
        var res = eval("input object region");
        assertEquals(Map.of("env", "dev", "region", "us-east-1"), res);
    }

    @Test
    void inputUnion() {
        setInput("hello");
        var res = eval("""
                type custom = string | number
                input custom region
                """);
        assertEquals("hello", res);
    }

    @Test
    void inputUnionNumber() {
        setInput(10);
        var res = eval("""
                type custom = string | number
                input custom region
                """);
        assertEquals(10, res);
    }

    /********************************
     * HAPPY CASES - default values *
     * ******************************/
    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputStringInit() {
        var res = eval("input string region = 'hello'");
        assertEquals("hello", res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputNumberInit() {
        var res = eval("input number region = 10 ");
        assertEquals(10, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputDecimalInit() {
        var res = eval("input number region = 10.2 ");
        assertEquals(10.2, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputDecimalHalfInit() {
        var res = eval("input number region = 0.2 ");
        assertEquals(0.2, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputBooleanInit() {
        var res = eval("input boolean region = true");
        assertEquals(true, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputObjectInitEmpty() {
        var res = eval("input object region = {}");
        assertEquals(Map.of(), res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputObjectInit() {
        var res = eval("input object region = {env : 'dev'}");
        assertEquals(Map.of("env", "dev"), res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputUnionInit() {
        var res = eval("""
                type custom = string | number
                input custom region = 10
                """);
        assertEquals(10, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void inputUnionInitString() {
        var res = eval("""
                type custom = string | number
                input custom region = "hello"
                """);
        assertEquals("hello", res);
    }

    @Test
    void inputStringInitWithNumberError() {
        setInput(10);
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithDecimalError() {
        setInput(10.1);
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void inputStringInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void inputNumberInitWithStringError() {
        setInput("hello");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void inputNumberInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void inputNumberInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("input number region"));
    }

    @Test
    void inputNumberInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("input number region"));
    }

    @Test
    void inputNumberInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void inputNumberInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void inputNumberInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("input number region ")); // throw because it's not declared as array
    }

    @Test
    void inputNumberInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void inputNumberInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void inputBooleanInitError() {
        setInput(123);
        assertThrows(TypeError.class, () -> eval("input boolean region"));
    }

    @Test
    void inputObjectInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("input object region"));
    }

    @Test
    void inputObjectInitWithStringError() {
        setInput("hello");
        assertThrows(TypeError.class, () -> eval("input object region = 'hello'"));
    }

    @Test
    void inputUnionInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom region 
                """));
    }

    @Test
    void inputStringArray() {
        setInput("['hello','world']");
        var res = eval("input string[] region");
        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void inputNumberArray() {
        setInput("[1,2,3]");
        var res = eval("input number[] region");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    @Disabled
    void inputNumberArrayNoParanthesis() {
        setInput("1,2,3");
        var res = eval("input number[] region");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void inputUnionArray() {
        setInput("['hello','world']");
        var res = eval("""
                type custom = string | number
                input custom[] region
                """);

        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void inputStringArrayInit() {
        var res = eval("input string[] region=['hi']");
        assertEquals(List.of("hi"), res);
    }

    @Test
    void inputNumberArrayInit() {
        var res = eval("input number[] region=[1,2,3]");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void inputBooleanArray() {
        setInput("[true,false,true]");
        var res = eval("input boolean[] region");
        assertEquals(List.of(true, false, true), res);
    }

    @Test
    void inputBooleanArrayInit() {
        var res = eval("input boolean[] region=[true,false,true]");
        assertEquals(List.of(true, false, true), res);
    }

    @Test
    void inputObjectArray() {
        setInput("[{env:'dev'}]");
        var res = eval("input object[] region");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }

    @Test
    void inputObjectArrayInit() {
        var res = eval("input object[] region=[{env:'dev'}]");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }


    @Test
    void inputUnionArrayInit() {
        var res = eval("""
                type custom = string | number
                input custom[] region = [10]
                """);
        assertEquals(List.of(10), res);
    }

    @Test
    void inputStringArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input string[] region=[1,2,3]"));
    }

    @Test
    void inputNumberArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input number[] region=['hi']"));
    }

    @Test
    void inputBooleanArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input boolean[] region=[1]"));
    }

    @Test
    void inputObjectArrayInitError() {
        assertThrows(TypeError.class, () -> eval("input object[] region=[1]"));
    }


    @Test
    void inputUnionArrayInitError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string | number
                input custom[] region = [true]
                """));
    }

}
