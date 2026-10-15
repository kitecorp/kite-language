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

    /**
     * A string holding double quotes, like the JSON an IAM policy is written in, keeps
     * every character: it used to be cut down to the text between its first two double
     * quotes, so '{"Version":"2012-10-17"}' became Version (kitecorp/kite#109).
     */
    @Test
    void singleQuotedJson_keepsItsQuotes() {
        eval("""
                var policy = '{"Version":"2012-10-17","Statement":[]}'
                """);
        Assertions.assertEquals("{\"Version\":\"2012-10-17\",\"Statement\":[]}", interpreter.getVar("policy"));
    }

    @Test
    void doubleQuotedTextWithEscapedQuotes_keepsThem() {
        eval("""
                var greeting = "say \\"hi\\" now"
                """);
        Assertions.assertEquals("say \"hi\" now", interpreter.getVar("greeting"));
    }

    @Test
    void singleQuotedText_thatIsAQuotation_keepsItsQuotes() {
        eval("""
                var quotation = '"hi"'
                """);
        Assertions.assertEquals("\"hi\"", interpreter.getVar("quotation"));
    }

    @Test
    void doubleQuotedText_thatIsAQuotation_keepsItsQuotes() {
        eval("""
                var quotation = "\\"hi\\""
                var single = "'abc'"
                """);
        Assertions.assertEquals(java.util.List.of("\"hi\"", "'abc'"),
                java.util.List.of(interpreter.getVar("quotation"), interpreter.getVar("single")));
    }
}
