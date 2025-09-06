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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Each subclass provides a different input type from a different source.
 * Valid sources:
 * 1. CLI
 * 2. File
 * 3. Environment variable
 * All tests will be run on the input type provided by the subclass.
 */
@Log4j2
public abstract class InputTests extends RuntimeTest {
    private ChainResolver chainResolver;
    private TypeChecker typeChecker;

    protected abstract void setInput(String input);

    protected abstract void setInput(Integer input);

    protected abstract void setInput(Boolean input);

    protected abstract void setInput(Double input);

    protected abstract void setInput(Float input);

    @Override
    protected void init() {
        this.global = new Environment<>();
        this.global.setName("global");
        this.parser = new Parser();
        this.tokenizer = new Tokenizer();
        this.typeChecker = new TypeChecker();
        Environment<Object> inputs = new Environment<>(global);
        inputs.setName("inputs");
        this.chainResolver = getChainResolver();
        this.scopeResolver = new ScopeResolver();
        this.interpreter = new Interpreter(global);
    }

    protected abstract @NotNull ChainResolver getChainResolver();

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
    void testInputString() {
        setInput("hello");
        var res = eval("input string region");
        assertEquals("hello", res);
    }

    @Test
    void testInputNumber() {
        setInput(10);
        var res = eval("input number region");
        assertEquals(10, res);
    }

    @Test
    void testInputDecimal() {
        setInput(10.2);
        var res = eval("input number region");
        assertEquals(10.2, res);
    }

    @Test
    void testInputDecimalHalf() {
        setInput(0.2);
        var res = eval("input number region");
        assertEquals(0.2, res);
    }

    @Test
    void testInputBoolean() {
        setInput(true);
        var res = eval("input boolean region");
        assertEquals(true, res);
    }

    @Test
    void testInputObject() {
        setInput("{ env : 'dev', region : 'us-east-1' }");
        var res = eval("input object region");
        assertEquals(Map.of("env", "dev", "region", "us-east-1"), res);
    }

    @Test
    void testInputUnion() {
        setInput("hello");
        var res = eval("""
                type custom = string | number
                input custom region
                """);
        assertEquals("hello", res);
    }

    @Test
    void testInputUnionNumber() {
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
    void testInputStringInit() {
        var res = eval("input string region = 'hello'");
        assertEquals("hello", res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void testInputNumberInit() {
        var res = eval("input number region = 10 ");
        assertEquals(10, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void testInputDecimalInit() {
        var res = eval("input number region = 10.2 ");
        assertEquals(10.2, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void testInputDecimalHalfInit() {
        var res = eval("input number region = 0.2 ");
        assertEquals(0.2, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void testInputBooleanInit() {
        var res = eval("input boolean region = true");
        assertEquals(true, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void testInputObjectInitEmpty() {
        var res = eval("input object region = {}");
        assertEquals(Map.of(), res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void testInputObjectInit() {
        var res = eval("input object region = {env : 'dev'}");
        assertEquals(Map.of("env", "dev"), res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void testInputUnionInit() {
        var res = eval("""
                type custom = string | number
                input custom region = 10
                """);
        assertEquals(10, res);
    }

    @Test
    @DisplayName("Should not prompt for input when default value is provided")
    void testInputUnionInitString() {
        var res = eval("""
                type custom = string | number
                input custom region = "hello"
                """);
        assertEquals("hello", res);
    }

    @Test
    void testInputStringArray() {
        setInput("['hello','world']");
        var res = eval("input string[] region");
        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void testInputNumberArray() {
        setInput("[1,2,3]");
        var res = eval("input number[] region");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void testInputNumberArrayNoParanthesis() {
        setInput("1,2,3");
        var res = eval("input number[] region");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void testInputUnionArray() {
        setInput("['hello','world']");
        var res = eval("""
                type custom = string | number
                input custom[] region
                """);

        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void testInputStringArrayInit() {
        var res = eval("input string[] region=['hi']");
        assertEquals(List.of("hi"), res);
    }

    @Test
    void testInputNumberArrayInit() {
        var res = eval("input number[] region=[1,2,3]");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void testInputBooleanArray() {
        setInput("[true,false,true]");
        var res = eval("input boolean[] region");
        assertEquals(List.of(true, false, true), res);
    }

    @Test
    void testInputBooleanArrayInit() {
        var res = eval("input boolean[] region=[true,false,true]");
        assertEquals(List.of(true, false, true), res);
    }

    @Test
    void testInputObjectArray() {
        setInput("[{env:'dev'}]");
        var res = eval("input object[] region");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }

    @Test
    void testInputObjectArrayInit() {
        var res = eval("input object[] region=[{env:'dev'}]");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }


    @Test
    void testInputUnionArrayInit() {
        var res = eval("""
                type custom = string | number
                input custom[] region = [10]
                """);
        assertEquals(List.of(10), res);
    }

    @Test
    void testInputUnionArrayInitString() {
        var res = eval("""
                type custom = string | number
                input custom[] region = ['hello']
                """);
        assertEquals(List.of("hello"), res);
    }

    @Test
    void testInputUnionArrayInitStringNumber() {
        var res = eval("""
                type custom = string | number
                input custom[] region = ['hello', 10]
                """);
        assertEquals(List.of("hello", 10), res);
    }


    /************************************************************
     * ERROR CASES                                             **
     * Each error case will also be followed up by its alias   **
     * **********************************************************/
    /*
     * String input with invalid values
     * */
    @Test
    void testInputStringInitWithNumberError() {
        setInput(10);
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithDecimalError() {
        setInput(10.1);
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithDecimalDotError() {
        setInput(0.1);
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    @Test
    void testInputStringInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("input string region"));
    }

    /*
     * input string alias with invalid values
     * */
    @Test
    void testInputStringAliasInitWithNumberError() {
        setInput(10);
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithDecimalError() {
        setInput(10.1);
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithDecimalDotError() {
        setInput(0.1);
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputStringAliasInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    // NUMBER
    @Test
    void testInputNumberInitWithStringError() {
        setInput("hello");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void testInputNumberInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void testInputNumberInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("input number region"));
    }

    @Test
    void testInputNumberInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("input number region"));
    }

    @Test
    void testInputNumberInitWithBlankStringError() {
        setInput("  ");
        assertThrows(MissingInputException.class, () -> eval("input number region"));
    }

    @Test
    void testInputNumberInitWithTabStringError() {
        setInput("\t");
        assertThrows(MissingInputException.class, () -> eval("input number region"));
    }

    @Test
    void testInputNumberInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void testInputNumberInitWithBlankArrayError() {
        setInput("[      ]");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void testInputNumberInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void testInputNumberInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("input number region ")); // throw because it's not declared as array
    }

    @Test
    void testInputNumberInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    @Test
    void testInputNumberInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("input number region "));
    }

    /*
     * input number alias with invalid values
     * */
    @Test
    void testInputNumberAliasInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    @Test
    void testInputNumberAliasInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    @Test
    void testInputNumberAliasInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    @Test
    void testInputNumberAliasInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    @Test
    void testInputNumberAliasInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    @Test
    void testInputNumberAliasInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    @Test
    void testInputNumberAliasInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    @Test
    void testInputNumberAliasInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    @Test
    void testInputNumberAliasInitWithObjectArrayKeyStringError() {
        setInput("[{ 'env' : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                input custom region
                """));
    }

    // BOOLEAN
    @Test
    void testInputBooleanInitWithNumberError() {
        setInput(123);
        assertThrows(TypeError.class, () -> eval("input boolean region"));
    }

    @Test
    void testInputBooleanInitWithDecimalError() {
        setInput(1.2);
        assertThrows(TypeError.class, () -> eval("input boolean region"));
    }

    @Test
    void testInputBooleanInitWithDecimalDotError() {
        setInput(0.2);
        assertThrows(TypeError.class, () -> eval("input boolean region"));
    }

    @Test
    void testInputBooleanInitWithStringError() {
        setInput("hello");
        assertThrows(TypeError.class, () -> eval("input boolean region "));
    }

    @Test
    void testInputBooleanInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("input boolean region"));
    }

    @Test
    void testInputBooleanInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("input boolean region"));
    }

    @Test
    void testInputBooleanInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("input boolean region "));
    }

    @Test
    void testInputBooleanInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("input boolean region "));
    }

    @Test
    void testInputBooleanInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("input boolean region ")); // throw because it's not declared as array
    }

    @Test
    void testInputBooleanInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("input boolean region "));
    }

    @Test
    void testInputBooleanInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("input boolean region "));
    }

    /*
     * input string alias with invalid values
     * */
    @Test
    void testInputBooleanAliasInitWithNumberError() {
        setInput(10);
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithDecimalError() {
        setInput(10.1);
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithDecimalDotError() {
        setInput(0.1);
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    @Test
    void testInputBooleanAliasInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                input custom region
                """));
    }

    // OBJECT
    @Test
    void testInputObjectInitWithNumberError() {
        setInput(123);
        assertThrows(TypeError.class, () -> eval("input object region"));
    }

    @Test
    void testInputObjectInitWithDecimalError() {
        setInput(1.2);
        assertThrows(TypeError.class, () -> eval("input object region"));
    }

    @Test
    void testInputObjectInitWithDecimalDotError() {
        setInput(0.2);
        assertThrows(TypeError.class, () -> eval("input object region"));
    }

    @Test
    void testInputObjectInitWithStringError() {
        setInput("hello");
        assertThrows(TypeError.class, () -> eval("input object region "));
    }

    @Test
    void testInputObjectInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("input object region"));
    }

    @Test
    void testInputObjectInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("input object region"));
    }

    @Test
    void testInputObjectInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("input object region"));
    }

    @Test
    void testInputObjectInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("input object region "));
    }

    @Test
    void testInputObjectInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("input object region "));
    }

    @Test
    void testInputObjectInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("input object region ")); // throw because it's not declared as array
    }

    @Test
    void testInputObjectInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("input object region "));
    }

    @Test
    void testInputObjectInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("input object region "));
    }

    // ANY
    @Test
    void testInputAnyInitWithNumberError() {
        setInput(123);
        eval("input any region");
    }

    @Test
    void testInputAnyInitWithDecimalError() {
        setInput(1.2);
        eval("input any region");
    }

    @Test
    void testInputAnyInitWithDecimalDotError() {
        setInput(0.2);
        eval("input any region");
    }

    @Test
    void testInputAnyInitWithStringError() {
        setInput("hello");
        eval("input any region ");
    }

    @Test
    void testInputAnyInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("input any region"));
    }

    @Test
    void testInputAnyInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("input any region"));
    }

    @Test
    void testInputAnyInitWithBooleanError() {
        setInput(true);
        eval("input any region");
    }

    @Test
    void testInputAnyInitWithEmptyArrayError() {
        setInput("[]");
        eval("input any region ");
    }

    @Test
    void testInputAnyInitWithStringArrayError() {
        setInput("['hello','world']");
        eval("input any region ");
    }

    @Test
    void testInputAnyInitWithIntArrayError() {
        setInput("[1,2,3]");
        eval("input any region "); // throw because it's not declared as arry
    }

    @Test
    void testInputAnyInitWithBooleanArrayError() {
        setInput("[true, false]");
        eval("input any region ");
    }

    @Test
    void testInputAnyInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        eval("input any region ");
    }

    /*
     * Mixed Type alias with invalid values
     * */
    @Test
    void testInputMixedAliasInitWithNumberError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                input custom region
                """));
    }

    @Test
    void testInputMixedAliasInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("""
                type custom = number | string 
                input custom region
                """));
    }

    @Test
    void testInputMixedAliasInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("""
                type custom = number | string 
                input custom region
                """));
    }

    @Test
    void testInputMixedAliasInitWithEmptyArrayError() {
        setInput("[]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                input custom region
                """));
    }

    @Test
    void testInputMixedAliasInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                input custom region
                """));
    }

    @Test
    void testInputMixedAliasInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                input custom region
                """));
    }

    @Test
    void testInputMixedAliasInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                input custom region
                """));
    }

    @Test
    void testInputMixedAliasInitWithObjectArrayError() {
        setInput("[{ env : 'dev' }]");
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                input custom region
                """));
    }

    @Test
    void testInputUnionArrayInitBooleanInput() {
        setInput("[true]");
        assertThrows(TypeError.class, () -> eval("""
                    type custom = string | number
                    input custom[] region
                """));
    }

    @Test
    void testInputUnionArrayInitBoolean() {
        assertThrows(TypeError.class, () -> eval("""
                    type custom = string | number
                    input custom[] region = [true]
                """));
    }

    /*
     * OBJECT ARRAY
     * */
    @Test
    void testInputArrayInitWithNumberError() {
        setInput(123);
        assertThrows(TypeError.class, () -> eval("input object[] region"));
    }

    @Test
    void testInputArrayInitWithDecimalError() {
        setInput(1.2);
        assertThrows(TypeError.class, () -> eval("input object[] region"));
    }

    @Test
    void testInputArrayInitWithDecimalDotError() {
        setInput(0.2);
        assertThrows(TypeError.class, () -> eval("input object[] region"));
    }

    @Test
    void testInputArrayInitWithStringError() {
        setInput("hello");
        assertThrows(TypeError.class, () -> eval("input object[] region "));
    }

    @Test
    void testInputArrayInitWithNewLineError() {
        setInput("\n");
        assertThrows(MissingInputException.class, () -> eval("input object[] region"));
    }

    @Test
    void testInputArrayInitWithEmptyStringError() {
        setInput("");
        assertThrows(MissingInputException.class, () -> eval("input object[] region"));
    }

    @Test
    void testInputArrayInitWithBooleanError() {
        setInput(true);
        assertThrows(TypeError.class, () -> eval("input object[] region"));
    }

    @Test
    void testInputArrayInitWithStringArrayError() {
        setInput("['hello','world']");
        assertThrows(TypeError.class, () -> eval("input object[] region "));
    }

    @Test
    void testInputArrayInitWithIntArrayError() {
        setInput("[1,2,3]");
        assertThrows(TypeError.class, () -> eval("input object[] region ")); // throw because it's not declared as array
    }

    @Test
    void testInputArrayInitWithBooleanArrayError() {
        setInput("[true, false]");
        assertThrows(TypeError.class, () -> eval("input object[] region "));
    }

}
