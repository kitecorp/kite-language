package io.kite.frontend.parser.errors;

import io.kite.frontend.lexer.Token;
import io.kite.frontend.lexer.TokenType;
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
