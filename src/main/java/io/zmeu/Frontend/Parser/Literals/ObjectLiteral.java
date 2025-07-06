package io.zmeu.Frontend.Parser.Literals;

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
    private TypeIdentifier type;
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


    public boolean hasType() {
        return type != null;
    }

    @Override
    public Object getVal() {
        return value;
    }
}