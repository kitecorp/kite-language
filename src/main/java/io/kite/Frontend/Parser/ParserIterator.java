package io.kite.Frontend.Parser;

import io.kite.Frontend.Lexer.Token;
import io.kite.Frontend.Lexer.TokenType;
import io.kite.ParserErrors;
import io.kite.Visitors.AstPrinter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.ListIterator;

@Log4j2
public class ParserIterator {
    private final List<Token> tokens;
    private final ListIterator<Token> iterator;
    private final AstPrinter astPrinter = new AstPrinter();

    @Getter
    @Setter
    private Token current;

    public ParserIterator(List<Token> tokens) {
        this.tokens = tokens;
        this.iterator = tokens.listIterator();
    }

    private static boolean isComplexType(ListIterator<Token> iterator) {
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (next.is(TokenType.Dot, TokenType.OpenBrackets, TokenType.CloseBrackets)) {
                continue;
            }
            return next.is(TokenType.Identifier);
        }
        return false;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    boolean IsLookAhead(int k, TokenType... type) {
        var iterator = this.tokens.listIterator(this.iterator.previousIndex() + 1);
        for (var i = 0; i < k && iterator.hasNext(); i++, iterator.next()) {
            Token token = lookAhead();
            if (token == null || token.is(type)) {
                return true;
            }
        }
        return false;
    }

    boolean IsLookAheadAfter(TokenType after, TokenType... type) {
        int index = this.iterator.previousIndex() + 1;
        var iterator = this.tokens.listIterator(index);
        while (iterator.hasNext()) {
            var token = iterator.next();
            if (token.isLineTerminator()) {
                break;
            }
            if (token.is(after)) {
                token = iterator.next();
                if (token.is(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean IsLookAheadAfter(TokenType after, TokenType endToken,  TokenType... type) {
        int index = this.iterator.previousIndex() + 1;
        var iterator = this.tokens.listIterator(index);
        while (iterator.hasNext()) {
            var token = iterator.next();
            if (token.is(endToken)) {
                break;
            }
            if (token.is(after)) {
                token = iterator.next();
                if (token.is(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean IsLookAhead(TokenType... type) {
        if (!iterator.hasNext()) {
            return false;
        }
        Token token = lookAhead();
        return token != null && token.is(type);
    }

    boolean IsLookAhead(List<TokenType> type) {
        for (TokenType tokenType : type) {
            if (IsLookAhead(tokenType)) {
                return true;
            }
        }
        return false;
    }

    Token lookAhead() {
        if (!iterator.hasNext()) {
            return null;
        }
        return tokens.get(iterator.nextIndex());
    }

    Token eat(String error, TokenType... type) {
        Token lookAhead = lookAhead();
        if (lookAhead.is(TokenType.EOF)) {
            throw ParserErrors.error(error, lookAhead, type);
        }
        if (!lookAhead.is(type)) {
            throw ParserErrors.error(error, lookAhead, type);
        }
        return eat();
    }

    Token eat(TokenType... type) {
        return eat("Unexpected token found", type);
    }

    Token eat() {
        current = next();
        return current;
    }

    Token next() {
        return iterator.next();
    }

    Token prev() {
        return iterator.previous();
    }

    public Token eatIf(TokenType token) {
        // if the line terminator was not eaten in parsing, we consume it here. LineTerminator could be consumed by some other rules
        if (IsLookAhead(token)) {
            return eat();
        }
        return null;
    }

    public void synchronize() {
        while (hasNext()) {
            var next = eat();
            if (!next.isLineTerminator()) {
                continue;
            }

            switch (next.type()) {
                case Resource, Fun, Var, /*Val,*/ For, While, EOF, CloseBraces -> {
                    return;
                }
            }
        }
    }

    public boolean hasType() {
        int index = this.iterator.previousIndex() + 1;
        var iterator = this.tokens.listIterator(index);
        if (!iterator.hasNext()) {
            return false;
        }
        var token = iterator.next();
        if (token.isLineTerminator()) {
            return false;
        }
        return switch (token.type()) {
            case Identifier -> isComplexType(iterator);
            case Object -> true;
            default -> false;
        };

    }
}
