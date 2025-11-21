package io.kite.utils;

import io.kite.syntax.parser.literals.BooleanLiteral;

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
