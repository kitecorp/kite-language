package io.kite.TypeChecker.Types;

import io.kite.Frontend.Lexer.Token;
import io.kite.Frontend.Lexer.TokenType;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Parser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.kite.Frontend.Lexer.TokenType.*;
import static io.kite.Frontend.Lexer.TokenType.Colon;

public class TypeParser {

    private final Parser parser;

    public TypeParser(Parser parser) {
        this.parser = parser;
    }

    public List<Type> OptParameterList() {
        return IsLookAhead(CloseParenthesis, Colon) ? Collections.emptyList() : ParameterList();
    }

    private List<Type> ParameterList() {
        var params = new ArrayList<Type>();
        do {
            params.add(FunType());
        } while (IsLookAhead(Comma, Colon) && eat(Comma) != null);

        return params;
    }

    private Type FunType() {
        if (IsLookAhead(Colon)) {
            var type = identifier();
            return type.getType();
        } else if (IsLookAhead(Identifier)) {
            var type = TypeIdentifier();
            return type.getType();
        }
        return null;
    }

    public TypeIdentifier identifier() {
        return switch (lookahead().type()) {
            case OpenParenthesis -> FunctionType();
            case Identifier -> TypeIdentifier();
            case Object -> {
                var token = (Token) eat(Object);
                yield TypeIdentifier.type(token.type().toString());
            }
            default -> null;
        };
    }

    private TypeIdentifier FunctionType() {
        parser.eat(OpenParenthesis, "Expected '(' but got: " + parser.lookAhead());
        var params = OptParameterList();  // optimisation idea: why create ParameterIdentifier then extract just the type
        parser.eat(CloseParenthesis, "Expected ')' but got: " + parser.lookAhead());
        parser.eat(Lambda, "Expected -> but got: " + parser.lookAhead());
        var returnType = identifier();

        var type = new FunType(params, returnType.getType());
        return TypeIdentifier.builder()
                .type(type)
                .build();
    }

    private Object eat(TokenType tokenType) {
        return parser.eat(tokenType);
    }

    private boolean IsLookAhead(TokenType... tokenType) {
        return parser.IsLookAhead(tokenType);
    }

    private Token lookahead() {
        return parser.lookAhead();
    }

    /**
     * Parse Type with prefix
     * Base.Nested
     *
     */
    @NotNull
    public TypeIdentifier TypeIdentifier() {
        var type = new StringBuilder();
        for (var next = parser.eat(TokenType.Identifier); ; next = parser.eat(TokenType.Identifier)) {
            switch (parser.lookAhead().type()) {
                case Dot -> {
                    type.append(next.value().toString());
                    type.append(".");
                    parser.eat(Dot);
                }
                case Colon -> { // :Type just eat and move on
                    parser.eat(Colon);
                }
                case null -> {
                }
                default -> {
                    type.append(next.value().toString());
                    return TypeIdentifier.type(type.toString());
                }
            }
        }
    }

}
