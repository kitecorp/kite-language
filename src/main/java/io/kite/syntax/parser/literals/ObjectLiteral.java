package io.kite.syntax.parser.literals;

import io.kite.syntax.ast.expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static io.kite.syntax.parser.literals.StringLiteral.string;

/**
 * A Object literal has the form of: { key: value } or empty string {}
 * ObjectLiteral
 * : StringLiteral
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectLiteral extends Literal {
    private Expression key;
    private Expression value;

    public ObjectLiteral() {
    }

    public ObjectLiteral(Expression key, Expression value) {
        this();
        setKey(key);
        this.value = value;
    }

    public static ObjectLiteral object(Expression key, Expression value) {
        return new ObjectLiteral(key, value);
    }

    public static ObjectLiteral object(String key, Expression value) {
        return new ObjectLiteral(StringLiteral.string(key), value);
    }

    public static ObjectLiteral object(String key, String value) {
        return new ObjectLiteral(StringLiteral.string(key), string(value));
    }

    public static ObjectLiteral object() {
        return new ObjectLiteral();
    }

    @Override
    public Object getVal() {
        return value;
    }

    public String keyString() {
        return switch (key) {
            case StringLiteral literal -> literal.getValue();
            case SymbolIdentifier identifier -> identifier.string();
            default -> null;
        };
    }

    public void setKey(Expression key) {
        if (key instanceof StringLiteral literal) {
            this.key = literal;
        } else if (key instanceof SymbolIdentifier identifier) {
            this.key = identifier;
        } else {
            throw new IllegalArgumentException("Invalid key type: " + key);
        }
    }

    public record ObjectLiteralPair(String key, Object value) {
        public ObjectLiteralPair(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public ObjectLiteralPair() {
            this(null, null);
        }

    }
}