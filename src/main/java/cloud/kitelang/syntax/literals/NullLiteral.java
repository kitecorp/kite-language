package cloud.kitelang.syntax.literals;

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
    private static NullLiteral instance;

    private NullLiteral() {
    }

    public synchronized static Literal nullLiteral() {
        if (instance == null) {
            instance = new NullLiteral();
        }
        return instance;
    }

    @Override
    public Object getVal() {
        return value;
    }

}
