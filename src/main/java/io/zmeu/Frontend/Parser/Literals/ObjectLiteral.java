package io.zmeu.Frontend.Parser.Literals;

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
    private StringLiteral key;
    private Literal value;

    public ObjectLiteral() {
    }

    public ObjectLiteral(StringLiteral key, Literal value) {
        this();
        this.key = key;
        this.value = value;
    }

    public static ObjectLiteral object(StringLiteral key, Literal value) {
        return new ObjectLiteral(key, value);
    }

    public static ObjectLiteral object() {
        return new ObjectLiteral();
    }

    @Override
    public Object getVal() {
        return value;
    }
}