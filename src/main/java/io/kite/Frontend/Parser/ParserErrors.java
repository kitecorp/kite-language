package io.kite.Frontend.Parser;

import io.kite.Frontend.Lexer.Token;
import io.kite.Frontend.Lexer.TokenType;
import io.kite.Frontend.Parser.errors.ErrorList;
import io.kite.Frontend.Parser.errors.ParseError;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParserErrors {
    @Getter
    private static final List<ParseError> errors = new ArrayList<>();

    public static ParseError error(String message, Token token, TokenType type) {
        ParseError parseError = ParseError.builder()
                .actual(token)
                .message(message)
                .expected(type)
                .build();
        errors.add(parseError);
        return parseError;
    }

    public static ParseError error(String message) {
        ParseError parseError = ParseError.builder()
                .message(message)
                .build();
        errors.add(parseError);
        return parseError;
    }

    public static ErrorList error(String message, Token tokens, TokenType... type) {
        var list = new ArrayList<ParseError>(type.length);
        for (TokenType it : type) {
            list.add(error(message, tokens, it));
        }
        return ErrorList.builder().errors(list).build();
    }

    public static void clear() {
        errors.clear();
    }

    public static boolean hadErrors() {
        return !errors.isEmpty();
    }

    public static String errors() {
        return errors.stream().map(ParseError::getMessage).collect(Collectors.joining("\n"));
    }

}
