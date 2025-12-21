package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.literals.ObjectLiteral.ObjectLiteralPair;
import static cloud.kitelang.syntax.literals.ObjectLiteral.object;
import static cloud.kitelang.syntax.literals.StringLiteral.string;

@Slf4j
public class LiteralStringTest extends RuntimeTest {

    @Test
    void stringLiteral() {
        var res = interpreter.visit("""
                "hello world!"
                """);
        Assertions.assertEquals("\"hello world!\"\n", res);
    }

    @Test
    void stringEscape() {
        var res = interpreter.visit("what\'s up?");
        Assertions.assertEquals("what's up?", res);
    }

    @Test
    void stringEscapeDoubleQuote() {
        var res = interpreter.visit("what\"s up?");
        Assertions.assertEquals("what\"s up?", res);
    }

    @Test
    void tab() {
        var res = interpreter.visit("\twhat's up?");
        Assertions.assertEquals("\twhat's up?", res);
    }

    @Test
    void stringLiteralMultiline() {
        var res = interpreter.visit("""
                "hello     
                
                
                world!"
                """);
        Assertions.assertEquals("""
                "hello     
                
                
                world!"
                """, res);
    }

    @Test
    void stringLiteralMultilineInline() {
        var res = interpreter.visit("""
                "hello     
                 world!"
                """);
        Assertions.assertEquals("\"hello\n world!\"\n", res);
    }

    @Test
    void stringLiteralMultilineInlineComments() {
        var res = interpreter.visit("""
                "hello     
                 world!
                 // comments
                 "
                """);
        Assertions.assertEquals("\"hello\n world!\n // comments\n \"\n", res);
    }

    @Test
    void stringLiteralMultilineSingleQuotes() {
        var res = interpreter.visit("""
                'hello
                
                
                world!'
                """);
        Assertions.assertEquals("""
                'hello
                
                
                world!'
                """, res);
    }

    @Test
    void stringLiterals() {
        var res = interpreter.visit("hello world!");
        Assertions.assertEquals("hello world!", res);
    }

    @Test
    void objectSingleEntryString() {
        var res = (ObjectLiteralPair) interpreter.visit(object("env", string("production")));
        Assertions.assertNotNull(res);
        Assertions.assertEquals("env", res.key());
        Assertions.assertEquals("production", res.value());
    }


}
