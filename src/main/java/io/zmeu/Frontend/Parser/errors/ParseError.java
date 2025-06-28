package io.zmeu.Frontend.Parser.errors;

import io.zmeu.Frontend.Lexer.Token;
import io.zmeu.Frontend.Lexer.TokenType;
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
