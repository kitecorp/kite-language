package io.zmeu.Frontend.Lexer;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

public record Token(Object value, TokenType type, Object raw, int line) {
    public Token(Object value, TokenType type, Object raw, int line) {
        this.value = validateValue(value, type);
        this.type = type;
        this.raw = raw;
        this.line = line;
    }

    public Token(Object value, TokenType type) {
        this(value, type, validateValue(value, type), 0);
    }

    public Token(Object value, TokenType type, int line) {
        this(value, type, validateValue(value.toString(), type), line);
    }

    public boolean is(String... list) {
        return ArrayUtils.contains(list, this.value);
    }

    private static Object validateValue(Object value, TokenType type) {
        return switch (type) {
            case Number -> NumberUtils.createNumber(value.toString());
            default -> value;
        };
    }

    public boolean is(@NotNull TokenType type) {
        return this.type == type;
    }

    public boolean isLineTerminator() {
        return this.is(TokenType.NewLine, TokenType.SemiColon);
    }

    public boolean is(@NotNull TokenType... type) {
        return ArrayUtils.contains(type, this.type);
    }

    public boolean isAssignment() {
        return is(TokenType.Equal, TokenType.Equal_Complex);
    }

    public boolean isLiteral() {
        return TokenType.isLiteral(this.type);
    }

    public static Token of(Object value, TokenType type, Object raw, int line) {
        return new Token(value, type, raw, line);
    }

}
