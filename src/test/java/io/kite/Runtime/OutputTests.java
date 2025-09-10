package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Lexical.ScopeResolver;
import io.kite.Frontend.Parse.Literals.ArrayTypeIdentifier;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Parser;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.exceptions.MissingOutputException;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.Types.AnyType;
import io.kite.TypeChecker.Types.StringType;
import io.kite.TypeChecker.Types.ValueType;
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
    void outputString() {
        var res = eval("output string something = 'hello'");
        assertEquals("hello", res);
    }

    @Test
    void outputNumber() {
        var res = eval("output number something = 10");
        assertEquals(10, res);
    }

    @Test
    void outputDecimal() {
        var res = eval("output number something = 10.2");
        assertEquals(10.2, res);
    }

    @Test
    void outputDecimalHalf() {
        var res = eval("output number something = 0.2");
        assertEquals(0.2, res);
    }

    @Test
    void outputTrue() {
        var res = eval("output boolean something = true");
        assertEquals(true, res);
    }

    @Test
    void outputFalse() {
        var res = eval("output boolean something = false");
        assertEquals(false, res);
    }

    @Test
    void outputUnion() {
        var res = eval("""
                type custom = string | number
                output custom something = "hello"
                """);
        assertEquals("hello", res);
    }

    @Test
    void outputUnionNumber() {
        var res = eval("""
                type custom = string | number
                output custom something = 10
                """);
        assertEquals(10, res);
    }

    @Test
    void outputUnionDecimal() {
        var res = eval("""
                type custom = string | number
                output custom something = 0.2
                """);
        assertEquals(0.2, res);
    }

    @Test
    @DisplayName("Should not prompt for output when default value is provided")
    void outputObjectInitEmpty() {
        var res = eval("output object something = {}");
        assertEquals(Map.of(), res);
    }

    @Test
    void outputObject() {
        var res = eval("output object something = { env : 'dev', something : 'us-east-1' } ");
        assertEquals(Map.of("env", "dev", "something", "us-east-1"), res);
    }

    /*
     * ARRAYS
     * */
    @Test
    void outputStringArrayDefault() {
        var res = eval("output string[] something = ['hello','world'] ");
        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void outputNumberArrayDefault() {
        var res = eval("output number[] something = [1, 2, 3]");
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void outputBooleanArrayDefault() {
        var res = eval("output boolean[] something=[]");
        assertEquals(List.of(), res);
    }

    @Test
    void outputStringArrayEmptyDefault() {
        var res = eval("output string[] something = [] ");
        assertEquals(List.of(), res);
    }

    @Test
    void outputNumberArrayEmptyDefault() {
        var res = eval("output number[] something = []");
        assertEquals(List.of(), res);
    }

    @Test
    void outputBooleanArrayEmptyDefault() {
        var res = eval("output boolean[] something=[]");
        assertEquals(List.of(), res);
    }

    @Test
    void outputUnionArrayStringDefault() {
        var res = eval("""
                type custom = string | number
                output custom[] something = ['hello','world']
                """);

        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void outputUnionArrayNumbersDefault() {
        var res = eval("""
                type custom = string | number
                output custom[] something = [1,2,3]
                """);

        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void outputUnionArrayNumbersEmptyDefault() {
        var res = eval("""
                type custom = string | number
                output custom[] something = []
                """);

        assertEquals(List.of(), res);
    }

    @Test
    void outputObjectArrayDefault() {
        var res = eval("output object[] something=[{env:'dev'}]");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }

    @Test
    void outputObjectArrayEmptyDefault() {
        var res = eval("output object[] something=[]");
        assertEquals(List.of(), res);
    }

    @Test
    void outputUnionArrayStringNumberDefault() {
        var res = eval("""
                type custom = string | number
                output custom[] something = ['hello', 10]
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
    void outputStringDefaultNewLineError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", TypeIdentifier.type(new StringType("\n")))));
    }

    @Test
    void outputStringDefaultEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", TypeIdentifier.type(new StringType("")))));
    }

    /*
     * output string alias with invalid values
     * */

    @Test
    void outputNumberDefaultNewLineError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", type(ValueType.Number), "\n")));
    }

    @Test
    void outputNumberDefaultEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", type(ValueType.Number), "")));
    }

    @Test
    void outputNumberDefaultMissingStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", type(ValueType.Number), "    ")));
    }

    @Test
    void outputNumberDefaultBlankStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", string("     "))));
    }

    @Test
    void outputNumberDefaultTabStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", type(ValueType.Number), "\t")));
    }

    // BOOLEAN
    @Test
    void outputBooleanDefaultNewLineError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", TypeIdentifier.type(ValueType.Boolean), "\n")));
    }

    @Test
    void outputBooleanDefaultEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", TypeIdentifier.type(ValueType.Boolean), "")));
    }

    /// ////////
    /// ANY ///
    /// ///////
    @Test
    void outputAnyDefaultNumberError() {
        var res = eval("output any something = 123");
        assertEquals(123, res);
    }

    @Test
    void outputAnyDefaultDecimalError() {
        var res = eval("output any something = 1.2");
        assertEquals(1.2, res);
    }

    @Test
    void outputAnyDefaultDecimalDotError() {
        var res = eval("output any something = 0.2");
        assertEquals(0.2, res);
    }

    @Test
    void outputAnyTrueDefaultError() {
        var res = eval("output any something = true");
        assertEquals(true, res);
    }

    @Test
    void outputAnyFalseDefaultError() {
        var res = eval("output any something = false");
        assertEquals(false, res);
    }

    @Test
    void outputAnyDefaultStringError() {
        var res = eval("output any something = 'hello'");
        assertEquals("hello", res);
    }

    @Test
    void outputAnyDefaultNewLineError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", type(AnyType.INSTANCE), "\n")));
    }

    @Test
    void outputAnyDefaultEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(output("something", type(AnyType.INSTANCE), "")));
    }

    @Test
    void outputAnyDefaultBooleanError() {
        var res = eval("output any something=true");
        assertEquals(true, res);
    }

    @Test
    void outputAnyDefaultEmptyArrayError() {
        var res = eval("output any something = []");
        assertEquals(List.of(), res);
    }

    @Test
    void outputAnyDefaultStringArrayError() {
        var res = eval("output any something = ['hello','world']");
        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void outputAnyDefaultIntArrayError() {
        var res = eval("output any something = [1,2,3]"); // throw because it's not declared as arry
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void outputAnyDefaultBooleanArrayError() {
        var res = eval("output any something = [true, false]");
        assertEquals(List.of(true, false), res);
    }

    @Test
    void outputAnyDefaultObjectArrayError() {
        var res = eval("output any something = [{ env : 'dev' }]");
        assertEquals(List.of(Map.of("env", "dev")), res);
    }

    /*
     * Mixed Type alias with invalid values
     * */

    @Test
    void outputArrayDefaultNewLineError() {
        assertThrows(MissingOutputException.class, () ->
                interpreter.visit(
                        output("something", type("custom"), string(""))
                )
        );
    }

    @Test
    void outputArrayDefaultEmptyStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(
                output("something", ArrayTypeIdentifier.arrayType("object"), "  ")
        ));
    }

    @Test
    void outputArrayDefaultNewlineStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(
                output("something", ArrayTypeIdentifier.arrayType("object"), "\n")
        ));
    }

    @Test
    void outputArrayDefaultTabStringError() {
        assertThrows(MissingOutputException.class, () -> interpreter.visit(
                output("something", ArrayTypeIdentifier.arrayType("object"), "\t")
        ));
    }
}
