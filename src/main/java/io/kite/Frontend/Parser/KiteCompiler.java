package io.kite.Frontend.Parser;

import io.kite.Frontend.Parser.generated.KiteLexer;
import io.kite.Frontend.Parser.generated.KiteParser;
import org.antlr.v4.runtime.*;

public class KiteCompiler {

    public Program parse(String source) {
        // Step 1: Tokenize
        var input = CharStreams.fromString(source);
        var lexer = new KiteLexer(input);
        var tokens = new CommonTokenStream(lexer);

        // Step 2: Parse
        var parser = new KiteParser(tokens);

        // Step 3: Error handling
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol,
                                    int line, int charPositionInLine,
                                    String msg, RecognitionException e) {
                throw new RuntimeException(
                        String.format("Parse error at line %d:%d - %s",
                                line, charPositionInLine, msg)
                );
            }
        });

        // Step 4: Generate parse tree
        var tree = parser.program();

        // Step 5: Build AST
        var builder = new KiteASTBuilder();
        return builder.visitProgram(tree);
    }
}