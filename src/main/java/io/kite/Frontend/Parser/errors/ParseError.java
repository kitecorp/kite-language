package io.kite.Frontend.Parser.errors;

import io.kite.Frontend.Lexer.Token;
import io.kite.Frontend.Lexer.TokenType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class ParseError extends RuntimeException{
    private Token actual;
    private TokenType expected;
    private String message;
}
