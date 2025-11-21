package io.kite.execution;

import io.kite.analysis.visitors.SyntaxPrinter;
import io.kite.base.RuntimeTest;
import io.kite.semantics.TypeChecker;
import io.kite.semantics.scope.ScopeResolver;
import io.kite.syntax.ast.KiteCompiler;
import io.kite.tool.theme.PlainTheme;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Each subclass provides a different output type from a different source.
 * Valid sources:
 * 1. CLI
 * 2. File
 * 3. Environment variable
 * All tests will be run on the output type provided by the subclass.
 */
@Log4j2
public class OutputPrintTests extends RuntimeTest {
    private TypeChecker typeChecker;

    @Override
    protected void init() {
        this.compiler = new KiteCompiler();
        this.printer = new SyntaxPrinter(new PlainTheme());
        this.typeChecker = new TypeChecker(printer);
        this.scopeResolver = new ScopeResolver();
        this.interpreter = new Interpreter(printer);
    }


    protected Object eval(String source) {
        program = parse(source);
        scopeResolver.resolve(program);
        typeChecker.visit(program);
        return interpreter.visit(program);
    }

    @Test
    void outputResourceString() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud string arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output string something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", "arn::"));
        var value = interpreter.printOutputs(main);
        assertEquals("""
                output string something = "arn::"
                """.trim(), value);
    }

    @Test
    void outputResourceNumber() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud number arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output number something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", 10));
        var value = interpreter.printOutputs(main);
        assertEquals("output number something = 10", value);
    }

    @Test
    void outputResourceDecimal() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud number arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output number something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", 0.2));
        var value = interpreter.printOutputs(main);
        assertEquals("output number something = 0.2", value);
    }

    @Test
    void outputResourceTrue() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud boolean arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output boolean something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", true));
        var value = interpreter.printOutputs(main);
        assertEquals("output boolean something = true", value);
    }

    @Test
    void outputResourceFalse() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud boolean arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output boolean something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", false));
        var value = interpreter.printOutputs(main);
        assertEquals("output boolean something = false", value);
    }

    @Test
    void outputResourceAnyTrue() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", false));
        var value = interpreter.printOutputs(main);
        assertEquals("output any something = false", value);
    }

    @Test
    void outputResourceAnyFalse() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", false));
        var value = interpreter.printOutputs(main);
        assertEquals("output any something = false", value);
    }

    @Test
    void outputResourceAnyString() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", "arn::"));
        var value = interpreter.printOutputs(main);
        assertEquals("""
                output any something = "arn::"
                """.trim(), value);

    }

    @Test
    void outputResourceAnyNumber() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", 10));
        var value = interpreter.printOutputs(main);
        assertEquals("output any something = 10", value);
    }

    @Test
    void outputResourceAnyDecimal() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", 0.2));
        var value = interpreter.printOutputs(main);
        assertEquals("output any something = 0.2", value);
    }

    @Test
    void outputResourceAnyArrayNumber() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(10)));
        var value = interpreter.printOutputs(main);
        assertEquals("output any[] something = [10]", value);
    }

    @Test
    void outputResourceAnyArrayDecimal() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(0.2)));
        var value = interpreter.printOutputs(main);
        assertEquals("output any[] something = [0.2]", value);
    }

    @Test
    void outputResourceAnyArrayString() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of("hello")));
        var value = interpreter.printOutputs(main);
        assertEquals("""
                output any[] something = ["hello"]""", value);
    }

    @Test
    void outputResourceAnyArrayTrue() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(true)));
        var value = interpreter.printOutputs(main);
        assertEquals("output any[] something = [true]", value);
    }

    @Test
    void outputResourceAnyArrayFalse() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud any[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output any[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(false)));
        var value = interpreter.printOutputs(main);
        assertEquals("output any[] something = [false]", value);
    }

    @Test
    void outputResourceNumberArray() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud number[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output number[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(10)));
        var value = interpreter.printOutputs(main);
        assertEquals("output number[] something = [10]", value);
    }

    @Test
    void outputResourceDecimalArray() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud number[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output number[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(0.2)));
        var value = interpreter.printOutputs(main);
        assertEquals("output number[] something = [0.2]", value);
    }

    @Test
    void outputResourceTrueArray() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud boolean[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output boolean[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(true)));
        var value = interpreter.printOutputs(main);
        assertEquals("output boolean[] something = [true]", value);
    }

    @Test
    void outputResourceFalseArray() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud boolean[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output boolean[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(false)));
        var value = interpreter.printOutputs(main);
        assertEquals("output boolean[] something = [false]", value);
    }

    @Test
    void outputResourceStringArray() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud string[] arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output string[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of("arn::")));
        var value = interpreter.printOutputs(main);
        assertEquals("""
                output string[] something = ["arn::"]""", value);
    }

    @Test
    void outputResourceObject() {
        var res = eval("""
                schema vm {
                    string name
                    @cloud object arn
                 }
                
                 resource vm main {
                   name     = 'prod'
                 }
                
                 output object something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", Map.of("env", "dev")));
        var value = interpreter.printOutputs(main);
        assertEquals("""
                output object something = {"env": "dev"}""", value);
    }

    @Test
    void outputResourceObjectArray() {
        var res = eval("""
                schema vm {
                    @cloud object[] arn
                 }
                
                 resource vm main {
                 }
                
                 output object[] something = main.arn
                """);
        Map<String, Map<String, Object>> main = Map.of("main", Map.of("arn", List.of(Map.of("env", "dev"))));
        var value = interpreter.printOutputs(main);
        assertEquals("""
                output object[] something = [{"env": "dev"}]""", value);
    }


}
