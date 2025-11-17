package io.kite.Frontend.Token;

import io.kite.Frontend.Parser.KiteCompiler;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

@Log4j2
@DisplayName("Tokenizer")
public class TokenizerTest {
    private KiteCompiler compiler;

    @BeforeEach
    void beforeEach() {
        compiler = new KiteCompiler();
    }

//    @Test
//    void testOneDigit() {
//        var result = parse("1");
//        Assertions.assertEquals(TokenType.Number, result.type());
//        Assertions.assertEquals(1, result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testMultipleDigits() {
//        var result = parse("422");
//        Assertions.assertEquals(TokenType.Number, result.type());
//        Assertions.assertEquals(422, result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testDecimal() {
//        var result = parse("1.2");
//        Assertions.assertEquals(TokenType.Number, result.type());
//        Assertions.assertEquals(1.2f, result.value());
//        log.info(result);
//    }
//
//
//    @Test
//    void testSpace() {
//        var result = parse("  ");
//        Assertions.assertEquals(TokenType.EOF, result.type());
//        log.info(result);
//    }
//
//    @Test
//    void testLiteralStringNumber() {
//        var result = parse("""
//                "422"
//                """);
//        Assertions.assertEquals(TokenType.String, result.type());
//        Assertions.assertEquals("422", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testLiteralString() {
//        var result = parse("\"hello\"");
//        Assertions.assertEquals(TokenType.String, result.type());
//        Assertions.assertEquals("hello", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testLineTerminator() {
//        var result = parse("\n");
//        Assertions.assertEquals(TokenType.NewLine, result.type());
//        Assertions.assertEquals("\n", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testPlus() {
//        var result = parse("+");
//        Assertions.assertEquals(TokenType.Plus, result.type());
//        Assertions.assertEquals("+", result.value());
//        log.info(result);
//
//    }
//
//    @Test
//    void testMinus() {
//        var result = parse("-");
//        Assertions.assertEquals(TokenType.Minus, result.type());
//        Assertions.assertEquals("-", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testMultiplication() {
//        var result = parse("*");
//        Assertions.assertEquals(TokenType.Multiply, result.type());
//        Assertions.assertEquals("*", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testModulo() {
//        var result = parse("%");
//        Assertions.assertEquals(TokenType.Modulo, result.type());
//        Assertions.assertEquals("%", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testLineTerminatorComplex() {
//        var result = parse("1+1\n");
//        Assertions.assertEquals(TokenType.NewLine, result.get(3).type());
//        Assertions.assertEquals("\n", result.get(3).value());
//        log.info(result);
//    }
//
//    @Test
//    void testMultilineComment() {
//        var result = parse("""
//                 /*
//                  * "hello"
//                  */
//                  "Str"
//                """);
//        Assertions.assertEquals(TokenType.String, result.type());
//        Assertions.assertEquals("Str", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testLiteralSingleQuoteString() {
//        var result = parse("'hello'");
//        Assertions.assertEquals(TokenType.String, result.type());
//        Assertions.assertEquals("hello", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testLiteralWhitespaceString() {
//        var result = parse("   42    ");
//        Assertions.assertEquals(TokenType.Number, result.type());
//        Assertions.assertEquals(42, result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testLiteralWhitespaceStringInside() {
//        var result = parse("   \"  42  \"    ");
//        Assertions.assertEquals(TokenType.String, result.type());
//        Assertions.assertEquals("  42  ", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testOpenParanthesis() {
//        var result = parse("(");
//        Assertions.assertEquals(TokenType.OpenParenthesis, result.type());
//        Assertions.assertEquals("(", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testCloseParanthesis() {
//        var result = parse(")");
//        Assertions.assertEquals(TokenType.CloseParenthesis, result.type());
//        Assertions.assertEquals(")", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testOpenBraces() {
//        var result = parse("{");
//        Assertions.assertEquals(TokenType.OpenBraces, result.type());
//        Assertions.assertEquals("{", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testCloseBraces() {
//        var result = parse("}");
//        Assertions.assertEquals(TokenType.CloseBraces, result.type());
//        Assertions.assertEquals("}", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testOpenBrackets() {
//        var result = parse("[");
//        Assertions.assertEquals(TokenType.OpenBrackets, result.type());
//        Assertions.assertEquals("[", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testCloseBrackets() {
//        var result = parse("]");
//        Assertions.assertEquals(TokenType.CloseBrackets, result.type());
//        Assertions.assertEquals("]", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testNotEquals() {
//        var result = parse("!=");
//        Assertions.assertEquals(TokenType.Equality_Operator, result.type());
//        Assertions.assertEquals("!=", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testEqualsEquals() {
//        var result = parse("==");
//        Assertions.assertEquals(TokenType.Equality_Operator, result.type());
//        Assertions.assertEquals("==", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testLessEquals() {
//        var result = parse("<=");
//        Assertions.assertEquals(TokenType.RelationalOperator, result.type());
//        Assertions.assertEquals("<=", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testLess() {
//        var result = parse("<");
//        Assertions.assertEquals(TokenType.RelationalOperator, result.type());
//        Assertions.assertEquals("<", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testGreater() {
//        var result = parse(">");
//        Assertions.assertEquals(TokenType.RelationalOperator, result.type());
//        Assertions.assertEquals(">", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testGreaterEquals() {
//        var result = parse(">=");
//        Assertions.assertEquals(TokenType.RelationalOperator, result.type());
//        Assertions.assertEquals(">=", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testDivision() {
//        var result = parse("/");
//        Assertions.assertEquals(TokenType.Division, result.type());
//        Assertions.assertEquals("/", result.value());
//        log.info(result);
//    }
//
//    // Complex strings
//    @Test
//    void testOpenBracesWithText() {
//        var result = parse("{ \"hey\" }");
//        Assertions.assertEquals(TokenType.OpenBraces, result.type());
//        Assertions.assertEquals("{", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testOpenNested() {
//        var result = parse("{ { \"hey\" ");
//        Assertions.assertEquals(TokenType.OpenBraces, result.get(0).type());
//        Assertions.assertEquals("{", result.get(0).value());
//        Assertions.assertEquals(TokenType.OpenBraces, result.get(1).type());
//        Assertions.assertEquals("{", result.get(1).value());
//        log.info(result);
//    }
//
//    private Program parse(String source) {
//        return compiler.parse(source);
//    }
//
//    /// /////// COMMENTS /////////
//    @Test
//    void testCommentIsIgnored() {
//        var result = parse("// a comment goes until the end of line \n");
//        Assertions.assertEquals("EOF", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testNumberOnNextLineAfterComment() {
//        var result = parse("""
//                // a comment goes until the end of line
//                10
//                """);
//        Assertions.assertEquals(TokenType.Number, result.type());
//        Assertions.assertEquals(10, result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testCommentIgnoredAfterVar() {
//        var result = parse("var x=23 // a comment goes until the end of line 10");
//        Assertions.assertEquals(TokenType.Var, result.get(0).type());
//        Assertions.assertEquals("var", result.get(0).value());
//        Assertions.assertEquals(TokenType.Identifier, result.get(1).type());
//        Assertions.assertEquals("x", result.get(1).value());
//        Assertions.assertEquals(TokenType.Equal, result.get(2).type());
//        Assertions.assertEquals("=", result.get(2).value());
//        Assertions.assertEquals(TokenType.Number, result.get(3).type());
//        Assertions.assertEquals(23, result.get(3).value());
//        Assertions.assertEquals(TokenType.EOF, result.get(4).type());
//        Assertions.assertEquals("EOF", result.get(4).value());
//        log.info(result);
//    }
//
//    @Test
//    void testUnknownIdentifier() {
//        var result = parse("tudor");
//        Assertions.assertEquals(TokenType.Identifier, result.type());
//        Assertions.assertEquals("tudor", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testKeywordVar() {
//        var result = parse("var");
//        Assertions.assertEquals(TokenType.Var, result.type());
//        Assertions.assertEquals("var", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testKeywordVal() {
//        var result = parse("val");
////        Assertions.assertEquals(TokenType.Val, result.type());
//        Assertions.assertEquals("val", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testKeywordObject() {
//        var result = parse("object");
//        Assertions.assertEquals(TokenType.Object, result.type());
//        Assertions.assertEquals("object", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testKeywordModule() {
//        var result = parse("component");
//        Assertions.assertEquals(TokenType.Component, result.type());
//        Assertions.assertEquals("component", result.value());
//        log.info(result);
//    }
//
//    @Test
//    void testComplex() {
//        var result = parse("var x=10");
//        Assertions.assertEquals(TokenType.Var, result.get(0).type());
//        Assertions.assertEquals("var", result.get(0).value());
//        Assertions.assertEquals(TokenType.Identifier, result.get(1).type());
//        Assertions.assertEquals("x", result.get(1).value());
//        Assertions.assertEquals(TokenType.Equal, result.get(2).type());
//        Assertions.assertEquals("=", result.get(2).value());
//        Assertions.assertEquals(TokenType.Number, result.get(3).type());
//        Assertions.assertEquals(10, result.get(3).value());
//        log.info(result);
//    }
//
//    @Test
//    void testComplexWithSpace() {
//        var result = parse("var xuru   =    10");
//        Assertions.assertEquals(TokenType.Var, result.get(0).type());
//        Assertions.assertEquals("var", result.get(0).value());
//        Assertions.assertEquals(TokenType.Identifier, result.get(1).type());
//        Assertions.assertEquals("xuru", result.get(1).value());
//        Assertions.assertEquals(TokenType.Equal, result.get(2).type());
//        Assertions.assertEquals("=", result.get(2).value());
//        Assertions.assertEquals(TokenType.Number, result.get(3).type());
//        Assertions.assertEquals(10, result.get(3).value());
//        log.info(result);
//    }
//
//    @Test
//    void testComplexWithSpaceWithName() {
//        var result = parse("var variable");
//        Assertions.assertEquals(TokenType.Var, result.getBody().get(0).type());
//        Assertions.assertEquals("var", result.getBody().get(0).value());
//        Assertions.assertEquals(TokenType.Identifier, result.getBody().get(1).type());
//        Assertions.assertEquals("variable", result.getBody().get(1).value());
//        log.info(result);
//    }
//
//    @Test
//    void testUnexpected() {
//        var result = parse("&");
//        log.info(result);
//    }

}
