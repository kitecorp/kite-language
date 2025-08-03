package io.kite.Runtime.exceptions;

import io.kite.Frontend.Lexer.Token;
import lombok.Getter;


public class RuntimeError extends RuntimeException {
    @Getter
    private Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
    public RuntimeError(String message) {
        super(message);
    }
}