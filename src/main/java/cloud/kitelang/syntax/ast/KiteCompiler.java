package cloud.kitelang.syntax.ast;

import cloud.kitelang.syntax.ast.generated.KiteLexer;
import cloud.kitelang.syntax.ast.generated.KiteParser;
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
                // Clean up the message
                String cleanMsg = msg;

                // Replace token names with readable descriptions
                cleanMsg = cleanMsg.replace("'\\n'", "newline");
                cleanMsg = cleanMsg.replace("{", "");
                cleanMsg = cleanMsg.replace("}", "");
                cleanMsg = cleanMsg.replace("<EOF>", "end of file");

                String improvedMsg = improveErrorMessage(cleanMsg, (Parser) recognizer, (Token) offendingSymbol);

                // Add context from the source
                String context = getErrorContext(source, line, charPositionInLine);

                throw new ValidationException(
                        String.format("Parse error at line %d:%d - %s%s",
                                line, charPositionInLine, improvedMsg, context)
                );
            }
        });

        var tree = parser.program();
        var builder = new KiteASTBuilder();
        return builder.visitProgram(tree);
    }

    private String getErrorContext(String source, int line, int charPos) {
        String[] lines = source.split("\n");
        if (line < 1 || line > lines.length) {
            return "";
        }

        String errorLine = lines[line - 1];

        // Trim but keep track of leading whitespace for accurate positioning
        String trimmed = errorLine.trim();

        // Build context string
        StringBuilder context = new StringBuilder();
        context.append("\n  ").append(trimmed);

        // Add pointer to error location
        context.append("\n  ");
        int adjustedPos = charPos - (errorLine.length() - trimmed.length());
        for (int i = 0; i < adjustedPos; i++) {
            context.append(" ");
        }
        context.append("^");

        return context.toString();
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