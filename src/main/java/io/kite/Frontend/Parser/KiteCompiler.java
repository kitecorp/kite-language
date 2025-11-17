package io.kite.Frontend.Parser;

import io.kite.Frontend.Parser.generated.KiteLexer;
import io.kite.Frontend.Parser.generated.KiteParser;
import org.antlr.v4.runtime.*;

import java.util.List;

public class KiteCompiler {

    public Program parse(String source) {
        var input = CharStreams.fromString(source);
        var lexer = new KiteLexer(input);
        var tokens = new CommonTokenStream(lexer);

        var parser = new KiteParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol,
                                    int line, int charPositionInLine,
                                    String msg, RecognitionException e) {

                String improvedMsg = improveErrorMessage(
                        msg,
                        (org.antlr.v4.runtime.Parser) recognizer,
                        (Token) offendingSymbol
                );

                throw new ValidationException(
                        String.format("Parse error at line %d:%d - %s",
                                line, charPositionInLine, improvedMsg)
                );
            }
        });

        var tree = parser.program();
        var builder = new KiteASTBuilder();
        return builder.visitProgram(tree);
    }

    private String improveErrorMessage(String originalMsg, Parser parser, Token token) {
        List<String> ruleStack = parser.getRuleInvocationStack();
        String rules = String.join(",", ruleStack);

        // Missing ')' - if we see '=' or '{' while parsing condition
        if ((rules.contains("ifStatement") ||
             rules.contains("whileStatement") ||
             rules.contains("forStatement")) &&
            (token.getType() == KiteLexer.ASSIGN ||
             token.getType() == KiteLexer.LBRACE) &&
            originalMsg.contains("expecting")) {
            return "unmatched '(' - use both '(' and ')' or neither";
        }

        // Mismatched ')' in if/while without '('
        if ((rules.contains("ifStatement") || rules.contains("whileStatement")) &&
            token.getType() == KiteLexer.RPAREN &&
            (originalMsg.contains("mismatched input ')'") ||
             originalMsg.contains("extraneous input ')'"))) {
            return "unmatched ')' - use both '(' and ')' or neither";
        }

        // 2. Missing '=' in output declaration
        if (rules.contains("outputDeclaration") &&
            originalMsg.contains("expecting '='")) {
            return "output declaration requires '=' and value";
        }

        // 3. Missing closing brace
        if (originalMsg.contains("expecting '}'") &&
            (rules.contains("blockExpression") ||
             rules.contains("objectDeclaration"))) {
            return "missing closing '}'";
        }

        // 4. Missing opening brace for blocks (if/while/for)
        if ((rules.contains("ifStatement") ||
             rules.contains("whileStatement")) &&
            originalMsg.contains("expecting '{'")) {
            return "if/while statements require block { }";
        }

        // 5. Missing closing bracket in arrays
        if (rules.contains("arrayExpression") &&
            originalMsg.contains("expecting ']'")) {
            return "missing ']' to close array";
        }
// Missing ']' in decorator arguments
        if (rules.contains("decoratorArgs") &&
            (token.getType() == KiteLexer.IDENTIFIER ||
             token.getType() == KiteLexer.LBRACE) &&
            originalMsg.contains("expecting") &&
            originalMsg.contains("']'")) {
            return "missing ']' to close decorator array argument";
        }

        // Missing ')' in decorator arguments
        if (rules.contains("decorator") &&
            token.getType() == KiteLexer.IDENTIFIER &&
            originalMsg.contains("expecting") &&
            originalMsg.contains("')'")) {
            return "missing ')' to close decorator arguments";
        }
        // Default: use ANTLR's message (good enough for edge cases)
        return originalMsg;
    }
}