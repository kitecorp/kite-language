package io.zmeu.Frontend.Parser.Literals;

import io.zmeu.Frontend.Parser.SyntaxError;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*
 * NumericLiteral
 *      : NUMBER
 *      ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BooleanLiteral extends Literal {
    private boolean value;

    private BooleanLiteral(boolean value) {
        this.value = value;
    }

    public static BooleanLiteral bool(boolean value) {
        return new BooleanLiteral(value);
    }

    public static BooleanLiteral bool(Object value) {
        if (value instanceof String string) {
            return new BooleanLiteral(Boolean.parseBoolean(string));
        } else if (value instanceof Boolean bool) {
            return new BooleanLiteral(bool);
        } else {
            throw new SyntaxError();
        }
    }

    @Override
    public Object getVal() {
        return value;
    }

}
