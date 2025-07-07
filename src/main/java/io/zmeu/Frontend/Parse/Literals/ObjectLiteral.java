package io.zmeu.Frontend.Parse.Literals;

import io.zmeu.Frontend.Parser.Expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A Object literal has the form of: { key: value } or empty string {}
 * ObjectLiteral
 * : StringLiteral
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ObjectLiteral extends Literal {
    private Identifier key;
    private Expression value;

    public ObjectLiteral() {
    }

    public ObjectLiteral(Identifier key, Expression value) {
        this();
        this.key = key;
        this.value = value;
    }

    public static ObjectLiteral object(Identifier key, Expression value) {
        return new ObjectLiteral(key, value);
    }

    public static ObjectLiteral object(String key, Expression value) {
        return new ObjectLiteral(Identifier.id(key), value);
    }

    public static ObjectLiteral object() {
        return new ObjectLiteral();
    }

    @Override
    public Object getVal() {
        return value;
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