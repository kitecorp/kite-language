package io.kite.Frontend.Parse.Literals;

import lombok.Data;
import lombok.EqualsAndHashCode;

/*
 * NumericLiteral
 *      : NUMBER
 *      ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NullLiteral extends Literal {
    private static final String value = "null";

    private NullLiteral() {
    }

    public static Literal nullLiteral() {
        return new NullLiteral();
    }

    @Override
    public Object getVal() {
        return value;
    }

}
