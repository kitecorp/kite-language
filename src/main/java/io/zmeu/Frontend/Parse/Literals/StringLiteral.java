package io.kite.Frontend.Parse.Literals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * A string literal has the form of: "hello" or empty string ""
 * StringLiteral
 * : STRING
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StringLiteral extends Literal {
    private String value;

    public StringLiteral() {
    }

    public StringLiteral(String value) {
        this();
        setValue(value);
    }

    public static StringLiteral of(String value) {
        return new StringLiteral(value);
    }

    public static StringLiteral string(String value) {
        return new StringLiteral(value);
    }

    private void setValue(String value) {
        if (StringUtils.isBlank(value)) {
            this.value = value;
        } else {
            this.value = LiteralUtils.quote(value);
        }
    }

    public StringLiteral(Object value) {
        this();
        if (value instanceof String s) {
            setValue(s);
        }
    }

    @Override
    public Object getVal() {
        return value;
    }

}
