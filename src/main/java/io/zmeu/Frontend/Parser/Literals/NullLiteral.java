package io.zmeu.Frontend.Parser.Literals;

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

    public static Literal of() {
        return new NullLiteral();
    }

    @Override
    public Object getVal() {
        return value;
    }

}
