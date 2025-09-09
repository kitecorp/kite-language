package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parse.Literals.ArrayTypeIdentifier;
import io.kite.Frontend.Parser.Parser;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.exceptions.MissingOutputException;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.TypeError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.kite.Frontend.Parse.Literals.StringLiteral.string;
import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.OutputDeclaration.output;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Each subclass provides a different output type from a different source.
 * Valid sources:
 * 1. CLI
 * 2. File
 * 3. Environment variable
 * All tests will be run on the output type provided by the subclass.
 */
@Log4j2
public class OutputTests extends RuntimeTest {
    private TypeChecker typeChecker;

    @Override
    protected void init() {
        this.global = new Environment<>();
        this.global.setName("global");
        this.parser = new Parser();
        this.tokenizer = new Tokenizer();
        this.typeChecker = new TypeChecker();
        this.scopeResolver = new ScopeResolver();
        this.interpreter = new Interpreter(global);
    }


    protected Object eval(String source) {
        program = src(source);
        scopeResolver.resolve(program);
        typeChecker.visit(program);
        return interpreter.visit(program);
    }

    /**************
     * HAPPY CASES *
     * *************/
    @Test
    void testOutputString() {
        var res = eval("output string region = 'hello'");
        assertEquals("hello", res);
    }

    @Test
    void testOutputNumber() {
        var res = eval("output number region = 10");
        assertEquals(10, res);
    }

    @Test
    void testOutputDecimal() {
        var res = eval("output number region = 10.2");
        assertEquals(10.2, res);
    }

    @Test
    void testOutputDecimalHalf() {
        var res = eval("output number region = 0.2");
        assertEquals(0.2, res);
    }

    @Test
    void testOutputTrue() {
        var res = eval("output boolean region = true");
        assertEquals(true, res);
    }

    @Test
    void testOutputFalse() {
        var res = eval("output boolean region = false");
        assertEquals(false, res);
    }

    @Test
    void testOutputUnion() {
        var res = eval("""
                type custom = string | number
                output custom region = "hello"
                """);
        assertEquals("hello", res);
    }

    @Test
    void testOutputUnionNumber() {
        var res = eval("""
                type custom = string | number
                output custom region = 10
                """);
        assertEquals(10, res);
    }

    @Test
    @DisplayName("Should not prompt for output when default value is provided")
    void testOutputObjectInitEmpty() {
        var res = eval("output object region = {}");
        assertEquals(Map.of(), res);
    }

    @Test
    void testOutputObject() {
        var res = eval("output object region = { env : 'dev', region : 'us-east-1' } ");
        assertEquals(Map.of("env", "dev", "region", "us-east-1"), res);
    }

    /*
     * ARRAYS
     * */
    @Test
    void testOutputStringArrayDefault() {
        var res = eval("output string[] region = ['hello','world'] ");
        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void testOutputNumberArrayDefault() {
        var res = eval("output number[] region = [1, 2, 3]");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void testOutputBooleanArrayDefault() {
        var res = eval("output boolean[] region=[]");
        assertEquals(List.of(true, false), res);
    }

    @Test
    void testOutputStringArrayEmptyDefault() {
        var res = eval("output string[] region = [] ");
        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void testOutputNumberArrayEmptyDefault() {
        var res = eval("output number[] region = []");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void testOutputBooleanArrayEmptyDefault() {
        var res = eval("output boolean[] region=[]");
        assertEquals(List.of(true, false), res);
    }

    @Test
    void testOutputUnionArrayStringDefault() {
        var res = eval("""
                type custom = string | number
                output custom[] region = ['hello','world']
                """);

        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void testOutputUnionArrayNumbersDefault() {
        var res = eval("""
                type custom = string | number
                output custom[] region = [1,2,3]
                """);

        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void testOutputUnionArrayNumbersEmptyDefault() {
        var res = eval("""
                type custom = string | number
                output custom[] region = []
                """);

        assertEquals(List.of(), res);
    }

    @Test
    void testOutputObjectArrayDefault() {
        var res = eval("output object[] region=[{env:'dev'}]");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }

    @Test
    void testOutputObjectArrayEmptyDefault() {
        var res = eval("output object[] region=[]");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }

    @Test
    void testOutputUnionArrayStringNumberDefault() {
        var res = eval("""
                type custom = string | number
                output custom[] region = ['hello', 10]
                """);
        assertEquals(List.of("hello", 10), res);
    }


    /************************************************************
     * ERROR CASES                                             **
     * Each error case will also be followed up by its alias   **
     * **********************************************************/
    /*
     * String output with invalid values
     * */
    @Test
    void testOutputStringInitWithNumberError() {
        assertThrows(TypeError.class, () -> eval("output string region = 10"));
    }

    @Test
    void testOutputStringInitWithDecimalError() {
        assertThrows(TypeError.class, () -> eval("output string region = 10.1"));
    }

    @Test
    void testOutputStringInitWithDecimalDotError() {
        assertThrows(TypeError.class, () -> eval("output string region = 0.1"));
    }

    @Test
    void testOutputStringInitWithBooleanError() {
        assertThrows(TypeError.class, () -> eval("output string region = true"));
    }

    @Test
    void testOutputStringInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                output string region =
                """));
    }

    @Test
    void testOutputStringInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("output string region="));
    }

    @Test
    void testOutputStringInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("output string region = []"));
    }

    @Test
    void testOutputStringInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("output string region = ['hello','world']"));
    }

    @Test
    void testOutputStringInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("output string region = [1,2,3]"));
    }

    @Test
    void testOutputStringInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("output string region = [true, false] "));
    }

    @Test
    void testOutputStringInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("output string region = [{ env : 'dev' }]"));
    }

    /*
     * output string alias with invalid values
     * */
    @Test
    void testOutputStringAliasInitWithNumberError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = 10
                """));
    }

    @Test
    void testOutputStringAliasInitWithDecimalError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = 10.1
                """));
    }

    @Test
    void testOutputStringAliasInitWithDecimalDotError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = 0.1
                """));
    }

    @Test
    void testOutputStringAliasInitWithBooleanError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = true
                """));
    }

    @Test
    void testOutputStringAliasInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                type custom = string
                output custom region
                
                """));
    }

    @Test
    void testOutputStringAliasInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                type custom = string
                output custom region = 
                """));
    }

    @Test
    void testOutputStringAliasInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = []
                """));
    }

    @Test
    void testOutputStringAliasInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = ['hello','world']
                """));
    }

    @Test
    void testOutputStringAliasInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = [1,2,3]
                """));
    }

    @Test
    void testOutputStringAliasInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = [true, false]
                """));
    }

    @Test
    void testOutputStringAliasInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = [{ env : 'dev' }]
                """));
    }

    // NUMBER
    @Test
    void testOutputNumberInitWithStringError() {
        assertThrows(TypeError.class, () -> eval("""
                output number region = "hello" 
                """));
    }

    @Test
    void testOutputNumberInitWithBooleanError() {
        assertThrows(TypeError.class, () -> eval("output number region = true "));
    }

    @Test
    void testOutputNumberInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                output number region = 
                """));
    }

    @Test
    void testOutputNumberInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("output number region="));
    }

    @Test
    void testOutputNumberInitWithMissingStringError() {
        assertThrows(MissingOutputException.class, () -> eval("output number region=     "));
    }

    @Test
    void testOutputNumberInitWithBlankStringError() {
        assertThrows(MissingOutputException.class, () -> eval("output number region='     '"));
    }

    @Test
    void testOutputNumberInitWithTabStringError() {
        assertThrows(MissingOutputException.class, () -> eval("output number region = \t"));
    }

    @Test
    void testOutputNumberInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("output number region = []"));
    }

    @Test
    void testOutputNumberInitWithBlankArrayError() {
        assertThrows(TypeError.class, () -> eval("output number region = [      ] "));
    }

    @Test
    void testOutputNumberInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("output number region = ['hello','world'] "));
    }

    @Test
    void testOutputNumberInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("output number region = [1,2,3]")); // throw because it's not declared as array
    }

    @Test
    void testOutputNumberInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("output number region = [true, false]"));
    }

    @Test
    void testOutputNumberInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("output number region = [{ env : 'dev' }]"));
    }

    /*
     * output number alias with invalid values
     * */
    @Test
    void testOutputNumberAliasInitWithBooleanError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = true
                """));
    }

    @Test
    void testOutputNumberAliasInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                type custom = number
                output custom region =
                """));
    }

    @Test
    void testOutputNumberAliasInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                type custom = number
                output custom region = 
                """));
    }

    @Test
    void testOutputNumberAliasInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = []
                """));
    }

    @Test
    void testOutputNumberAliasInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = ['hello','world']
                """));
    }

    @Test
    void testOutputNumberAliasInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = [1,2,3]
                """));
    }

    @Test
    void testOutputNumberAliasInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = [true, false]
                """));
    }

    @Test
    void testOutputNumberAliasInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = [{ env : 'dev' }]
                """));
    }

    @Test
    void testOutputNumberAliasInitWithObjectArrayKeyStringError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number
                output custom region = [{ 'env' : 'dev' }]
                """));
    }

    // BOOLEAN
    @Test
    void testOutputBooleanInitWithNumberError() {
        assertThrows(TypeError.class, () -> eval("output boolean region = 123"));
    }

    @Test
    void testOutputBooleanInitWithDecimalError() {
        assertThrows(TypeError.class, () -> eval("output boolean region = 1.2"));
    }

    @Test
    void testOutputBooleanInitWithDecimalDotError() {
        assertThrows(TypeError.class, () -> eval("output boolean region = 0.2"));
    }

    @Test
    void testOutputBooleanInitWithStringError() {
        assertThrows(TypeError.class, () -> eval("""
                output boolean region = "hello" 
                """));
    }

    @Test
    void testOutputBooleanInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                output boolean region = 
                """));
    }

    @Test
    void testOutputBooleanInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("output boolean region = "));
    }

    @Test
    void testOutputBooleanInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("output boolean region = []"));
    }

    @Test
    void testOutputBooleanInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("output boolean region = ['hello','world']"));
    }

    @Test
    void testOutputBooleanInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("output boolean region = [1,2,3]")); // throw because it's not declared as array
    }

    @Test
    void testOutputBooleanInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("output boolean region = [true, false]"));
    }

    @Test
    void testOutputBooleanInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("output boolean region = [{ env : 'dev' }]"));
    }

    /*
     * output string alias with invalid values
     * */
    @Test
    void testOutputBooleanAliasInitWithNumberError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = string
                output custom region = 10
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithDecimalError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                output custom region = 10.1
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithDecimalDotError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                output custom region = 0.1
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                type custom = boolean
                output custom region = 
                
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                type custom = boolean
                output custom region = 
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                output custom region = []
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                output custom region = ['hello','world']
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                output custom region = [1,2,3]
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                output custom region = [true, false]
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                output custom region = [{ env : 'dev' }]
                """));
    }

    @Test
    void testOutputBooleanAliasInitWithObjectError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = boolean
                output custom region = { env : 'dev' }
                """));
    }

    // OBJECT
    @Test
    void testOutputObjectInitWithNumberError() {
        assertThrows(TypeError.class, () -> eval("output object region = 123"));
    }

    @Test
    void testOutputObjectInitWithDecimalError() {
        assertThrows(TypeError.class, () -> eval("output object region = 1.2"));
    }

    @Test
    void testOutputObjectInitWithDecimalDotError() {
        assertThrows(TypeError.class, () -> eval("output object region = 0.2"));
    }

    @Test
    void testOutputObjectInitWithStringError() {
        assertThrows(TypeError.class, () -> eval("output object region ='hello'"));
    }

    @Test
    void testOutputObjectInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                output object region=
                """));
    }

    @Test
    void testOutputObjectInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("output object region="));
    }

    @Test
    void testOutputObjectInitWithBooleanError() {
        assertThrows(TypeError.class, () -> eval("output object region=true"));
    }

    @Test
    void testOutputObjectInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("output object region = []"));
    }

    @Test
    void testOutputObjectInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("output object region = ['hello','world']"));
    }

    @Test
    void testOutputObjectInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("output object region = [1,2,3]")); // throw because it's not declared as array
    }

    @Test
    void testOutputObjectInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("output object region = [true, false]"));
    }

    @Test
    void testOutputObjectInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("output object region = [{ env : 'dev' }]"));
    }

    // ANY
    @Test
    void testOutputAnyInitWithNumberError() {
        var res = eval("output any region = 123");
        assertEquals(123, res);
    }

    @Test
    void testOutputAnyInitWithDecimalError() {
        var res = eval("output any region = 1.2");
        assertEquals(1.2, res);
    }

    @Test
    void testOutputAnyInitWithDecimalDotError() {
        var res = eval("output any region = 0.2");
        assertEquals(0.2, res);
    }

    @Test
    void testOutputAnyInitWithStringError() {
        var res = eval("output any region = 'hello'");
        assertEquals("hello", res);
    }

    @Test
    void testOutputAnyInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                output any region=
                """));
    }

    @Test
    void testOutputAnyInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("output any region="));
    }

    @Test
    void testOutputAnyInitWithBooleanError() {
        var res = eval("output any region=true");
        assertEquals(true, res);
    }

    @Test
    void testOutputAnyInitWithEmptyArrayError() {
        var res = eval("output any region = []");
        assertEquals(List.of(), res);
    }

    @Test
    void testOutputAnyInitWithStringArrayError() {
        var res = eval("output any region = ['hello','world']");
        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void testOutputAnyInitWithIntArrayError() {
        var res = eval("output any region = [1,2,3]"); // throw because it's not declared as arry
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void testOutputAnyInitWithBooleanArrayError() {
        var res = eval("output any region = [true, false]");
        assertEquals(List.of(true, false), res);
    }

    @Test
    void testOutputAnyInitWithObjectArrayError() {
        var res = eval("output any region = [{ env : 'dev' }]");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }

    /*
     * Mixed Type alias with invalid values
     * */
    @Test
    void testOutputMixedAliasInitWithNumberError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                output custom region = true
                """));
    }

    @Test
    void testOutputMixedAliasInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                type custom = number | string 
                output custom region =
                
                """));
    }

    @Test
    void testOutputMixedAliasInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> eval("""
                type custom = number | string 
                output custom region = 
                """));
    }

    @Test
    void testOutputMixedAliasInitWithEmptyArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                output custom region = []
                """));
    }

    @Test
    void testOutputMixedAliasInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                output custom region = ['hello','world']
                """));
    }

    @Test
    void testOutputMixedAliasInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                output custom region = [1,2,3]
                """));
    }

    @Test
    void testOutputMixedAliasInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                output custom region = [true, false]
                """));
    }

    @Test
    void testOutputMixedAliasInitWithObjectArrayError() {
        assertThrows(TypeError.class, () -> eval("""
                type custom = number | string 
                output custom region = [{ env : 'dev' }]
                """));
    }

    @Test
    void testOutputUnionArrayTrueDefault() {
        assertThrows(TypeError.class, () -> eval("""
                    type custom = string | number
                    output custom[] region = [true]
                """));
    }

    @Test
    void testOutputUnionArrayFalseDefault() {
        assertThrows(TypeError.class, () -> eval("""
                    type custom = string | number
                    output custom[] region = [false]
                """));
    }

    /*
     * OBJECT ARRAY
     * */
    @Test
    void testOutputArrayInitWithNumberError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = 123"));
    }

    @Test
    void testOutputArrayInitWithDecimalError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = 1.2"));
    }

    @Test
    void testOutputArrayInitWithDecimalDotError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = 0.2"));
    }

    @Test
    void testOutputArrayInitWithStringError() {
        assertThrows(TypeError.class, () -> eval("""
                output object[] region = 'hello'
                """));
    }

    @Test
    void testOutputArrayInitWithNewLineError() {
        assertThrows(MissingOutputException.class, () ->
                interpreter.visit(
                        output("region", type("custom"), string(""))
                )
        );
    }

    @Test
    void testOutputArrayInitWithEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(
                output("region", ArrayTypeIdentifier.arrayType("object"), "  ")
        ));
    }

    @Test
    void testOutputArrayInitWithBooleanError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = true"));
    }

    @Test
    void testOutputArrayInitWithStringArrayError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = ['hello','world']"));
    }

    @Test
    void testOutputArrayInitWithIntArrayError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = [1,2,3]")); // throw because it's not declared as array
    }

    @Test
    void testOutputArrayInitWithBooleanArrayError() {
        assertThrows(TypeError.class, () -> eval("output object[] region = [true, false]"));
    }

}
