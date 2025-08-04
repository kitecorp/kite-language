package io.kite.Utils;

import io.kite.Frontend.Parse.Literals.BooleanLiteral;

public class BoolUtils {
    public static boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        } else if (object instanceof Boolean b) {
            return b;
        } else {
            return object instanceof BooleanLiteral;
        }
    }
}
