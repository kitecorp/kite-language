package io.zmeu.Frontend.Token;

import io.zmeu.Frontend.Lexer.TokenType;
import io.zmeu.Frontend.Lexer.Tokenizer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Log4j2
@DisplayName("Tokenizer")
public class TokenizerTest {
    private Tokenizer tokenizer;

    @BeforeEach
    void beforeEach() {
        tokenizer = new Tokenizer();
    }

    @Test
    void testOneDigit() {
        var result = tokenizer.tokenizeLiteral("1");
        Assertions.assertEquals(TokenType.Number, result.type());
        Assertions.assertEquals(1, result.value());
        log.info(result);
    }

    @Test
    void testMultipleDigits() {
        var result = tokenizer.tokenizeLiteral("422");
        Assertions.assertEquals(TokenType.Number, result.type());
        Assertions.assertEquals(422, result.value());
        log.info(result);
    }

    @Test
    void testDecimal() {
        var result = tokenizer.tokenizeLiteral("1.2");
        Assertions.assertEquals(TokenType.Number, result.type());
        Assertions.assertEquals(1.2f, result.value());
        log.info(result);
    }


    @Test
    void testSpace() {
        var result = tokenizer.tokenizeLiteral("  ");
        Assertions.assertEquals(TokenType.EOF, result.type());
        log.info(result);
    }

    @Test
    void testLiteralStringNumber() {
        var result = tokenizer.tokenizeLiteral("""
                "422"
                """);
        Assertions.assertEquals(TokenType.String, result.type());
        Assertions.assertEquals("\"422\"", result.value());
        log.info(result);
    }

    @Test
    void testLiteralString() {
        var result = tokenizer.tokenizeLiteral("\"hello\"");
        Assertions.assertEquals(TokenType.String, result.type());
        Assertions.assertEquals("\"hello\"", result.value());
        log.info(result);
    }

    @Test
    void testLineTerminator() {
        var result = tokenizer.tokenizeLiteral("\n");
        Assertions.assertEquals(TokenType.NewLine, result.type());
        Assertions.assertEquals("\n", result.value());
        log.info(result);
    }

    @Test
    void testPlus() {
        var result = tokenizer.tokenizeLiteral("+");
        Assertions.assertEquals(TokenType.Plus, result.type());
        Assertions.assertEquals("+", result.value());
        log.info(result);

    }
    @Test
    void testMinus() {
        var result = tokenizer.tokenizeLiteral("-");
        Assertions.assertEquals(TokenType.Minus, result.type());
        Assertions.assertEquals("-", result.value());
        log.info(result);
    }
    @Test
    void testMultiplication() {
        var result = tokenizer.tokenizeLiteral("*");
        Assertions.assertEquals(TokenType.Multiply, result.type());
        Assertions.assertEquals("*", result.value());
        log.info(result);
    }

    @Test
    void testModulo() {
        var result = tokenizer.tokenizeLiteral("%");
        Assertions.assertEquals(TokenType.Modulo, result.type());
        Assertions.assertEquals("%", result.value());
        log.info(result);
    }

    @Test
    void testLineTerminatorComplex() {
        var result = tokenizer.tokenize("1+1\n");
        Assertions.assertEquals(TokenType.NewLine, result.get(3).type());
        Assertions.assertEquals("\n", result.get(3).value());
        log.info(result);
    }

    @Test
    void testMultilineComment() {
        var result = tokenizer.tokenizeLiteral("""
                 /* 
                  * "hello" 
                  */
                  "Str"
                """);
        Assertions.assertEquals(TokenType.String, result.type());
        Assertions.assertEquals("\"Str\"", result.value());
        log.info(result);
    }

    @Test
    void testLiteralSingleQuoteString() {
        var result = tokenizer.tokenizeLiteral("'hello'");
        Assertions.assertEquals(TokenType.String, result.type());
        Assertions.assertEquals("'hello'", result.value());
        log.info(result);
    }

    @Test
    void testLiteralWhitespaceString() {
        var result = tokenizer.tokenizeLiteral("   42    ");
        Assertions.assertEquals(TokenType.Number, result.type());
        Assertions.assertEquals(42, result.value());
        log.info(result);
    }

    @Test
    void testLiteralWhitespaceStringInside() {
        var result = tokenizer.tokenizeLiteral("   \"  42  \"    ");
        Assertions.assertEquals(TokenType.String, result.type());
        Assertions.assertEquals("\"  42  \"", result.value());
        log.info(result);
    }

    @Test
    void testOpenParanthesis() {
        var result = tokenizer.tokenizeLiteral("(");
        Assertions.assertEquals(TokenType.OpenParenthesis, result.type());
        Assertions.assertEquals("(", result.value());
        log.info(result);
    }

    @Test
    void testCloseParanthesis() {
        var result = tokenizer.tokenizeLiteral(")");
        Assertions.assertEquals(TokenType.CloseParenthesis, result.type());
        Assertions.assertEquals(")", result.value());
        log.info(result);
    }

    @Test
    void testOpenBraces() {
        var result = tokenizer.tokenizeLiteral("{");
        Assertions.assertEquals(TokenType.OpenBraces, result.type());
        Assertions.assertEquals("{", result.value());
        log.info(result);
    }

    @Test
    void testCloseBraces() {
        var result = tokenizer.tokenizeLiteral("}");
        Assertions.assertEquals(TokenType.CloseBraces, result.type());
        Assertions.assertEquals("}", result.value());
        log.info(result);
    }
    @Test
    void testOpenBrackets() {
        var result = tokenizer.tokenizeLiteral("[");
        Assertions.assertEquals(TokenType.OpenBrackets, result.type());
        Assertions.assertEquals("[", result.value());
        log.info(result);
    }

    @Test
    void testCloseBrackets() {
        var result = tokenizer.tokenizeLiteral("]");
        Assertions.assertEquals(TokenType.CloseBrackets, result.type());
        Assertions.assertEquals("]", result.value());
        log.info(result);
    }

    @Test
    void testNotEquals() {
        var result = tokenizer.tokenizeLiteral("!=");
        Assertions.assertEquals(TokenType.Equality_Operator, result.type());
        Assertions.assertEquals("!=", result.value());
        log.info(result);
    }

    @Test
    void testEqualsEquals() {
        var result = tokenizer.tokenizeLiteral("==");
        Assertions.assertEquals(TokenType.Equality_Operator, result.type());
        Assertions.assertEquals("==", result.value());
        log.info(result);
    }

    @Test
    void testLessEquals() {
        var result = tokenizer.tokenizeLiteral("<=");
        Assertions.assertEquals(TokenType.RelationalOperator, result.type());
        Assertions.assertEquals("<=", result.value());
        log.info(result);
    }

    @Test
    void testLess() {
        var result = tokenizer.tokenizeLiteral("<");
        Assertions.assertEquals(TokenType.RelationalOperator, result.type());
        Assertions.assertEquals("<", result.value());
        log.info(result);
    }

    @Test
    void testGreater() {
        var result = tokenizer.tokenizeLiteral(">");
        Assertions.assertEquals(TokenType.RelationalOperator, result.type());
        Assertions.assertEquals(">", result.value());
        log.info(result);
    }

    @Test
    void testGreaterEquals() {
        var result = tokenizer.tokenizeLiteral(">=");
        Assertions.assertEquals(TokenType.RelationalOperator, result.type());
        Assertions.assertEquals(">=", result.value());
        log.info(result);
    }

    @Test
    void testDivision() {
        var result = tokenizer.tokenizeLiteral("/");
        Assertions.assertEquals(TokenType.Division, result.type());
        Assertions.assertEquals("/", result.value());
        log.info(result);
    }
// Complex strings
    @Test
    void testOpenBracesWithText() {
        var result = tokenizer.tokenizeLiteral("{ \"hey\" }");
        Assertions.assertEquals(TokenType.OpenBraces, result.type());
        Assertions.assertEquals("{", result.value());
        log.info(result);
    }
    @Test
    void testOpenNested() {
        var result = tokenizer.tokenize("{ { \"hey\" ");
        Assertions.assertEquals(TokenType.OpenBraces, result.get(0).type());
        Assertions.assertEquals("{", result.get(0).value());
        Assertions.assertEquals(TokenType.OpenBraces, result.get(1).type());
        Assertions.assertEquals("{", result.get(1).value());
        log.info(result);
    }

    ////////// COMMENTS /////////
    @Test
    void testCommentIsIgnored() {
        var result = tokenizer.tokenizeLiteral("// a comment goes until the end of line \n");
        Assertions.assertEquals("EOF", result.value());
        log.info(result);
    }

    @Test
    void testNumberOnNextLineAfterComment() {
        var result = tokenizer.tokenizeLiteral("""
                // a comment goes until the end of line 
                10
                """);
        Assertions.assertEquals(TokenType.Number, result.type());
        Assertions.assertEquals(10, result.value());
        log.info(result);
    }

    @Test
    void testCommentIgnoredAfterVar() {
        var result = tokenizer.tokenize("var x=23 // a comment goes until the end of line 10");
        Assertions.assertEquals(TokenType.Var, result.get(0).type());
        Assertions.assertEquals("var", result.get(0).value());
        Assertions.assertEquals(TokenType.Identifier, result.get(1).type());
        Assertions.assertEquals("x", result.get(1).value());
        Assertions.assertEquals(TokenType.Equal, result.get(2).type());
        Assertions.assertEquals("=", result.get(2).value());
        Assertions.assertEquals(TokenType.Number, result.get(3).type());
        Assertions.assertEquals(23, result.get(3).value());
        Assertions.assertEquals(TokenType.EOF, result.get(4).type());
        Assertions.assertEquals("EOF", result.get(4).value());
        log.info(result);
    }

    @Test
    void testUnknownIdentifier() {
        var result = tokenizer.tokenizeLiteral("tudor");
        Assertions.assertEquals(TokenType.Identifier, result.type());
        Assertions.assertEquals("tudor", result.value());
        log.info(result);
    }

    @Test
    void testKeywordVar() {
        var result = tokenizer.tokenizeLiteral("var");
        Assertions.assertEquals(TokenType.Var, result.type());
        Assertions.assertEquals("var", result.value());
        log.info(result);
    }

    @Test
    void testKeywordModule() {
        var result = tokenizer.tokenizeLiteral("module");
        Assertions.assertEquals(TokenType.Module, result.type());
        Assertions.assertEquals("module", result.value());
        log.info(result);
    }

    @Test
    void testComplex() {
        var result = tokenizer.tokenize("var x=10");
        Assertions.assertEquals(TokenType.Var, result.get(0).type());
        Assertions.assertEquals("var", result.get(0).value());
        Assertions.assertEquals(TokenType.Identifier, result.get(1).type());
        Assertions.assertEquals("x", result.get(1).value());
        Assertions.assertEquals(TokenType.Equal, result.get(2).type());
        Assertions.assertEquals("=", result.get(2).value());
        Assertions.assertEquals(TokenType.Number, result.get(3).type());
        Assertions.assertEquals(10, result.get(3).value());
        log.info(result);
    }

    @Test
    void testComplexWithSpace() {
        var result = tokenizer.tokenize("var xuru   =    10");
        Assertions.assertEquals(TokenType.Var, result.get(0).type());
        Assertions.assertEquals("var", result.get(0).value());
        Assertions.assertEquals(TokenType.Identifier, result.get(1).type());
        Assertions.assertEquals("xuru", result.get(1).value());
        Assertions.assertEquals(TokenType.Equal, result.get(2).type());
        Assertions.assertEquals("=", result.get(2).value());
        Assertions.assertEquals(TokenType.Number, result.get(3).type());
        Assertions.assertEquals(10, result.get(3).value());
        log.info(result);
    }

    @Test
    void testComplexWithSpaceWithName() {
        var result = tokenizer.tokenize("var variable");
        Assertions.assertEquals(TokenType.Var, result.get(0).type());
        Assertions.assertEquals("var", result.get(0).value());
        Assertions.assertEquals(TokenType.Identifier, result.get(1).type());
        Assertions.assertEquals("variable", result.get(1).value());
        log.info(result);
    }

    @Test
    void testUnexpected() {
        var result = tokenizer.tokenizeLiteral("&");
        log.info(result);
    }

}
