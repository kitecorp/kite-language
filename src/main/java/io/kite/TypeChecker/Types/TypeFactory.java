package io.kite.TypeChecker.Types;

import org.apache.commons.lang3.StringUtils;

public class TypeFactory {
    private final static FunStore funStore = FunStore.getInstance();

    public static Type fromString(String symbol) {
        if (isFunction(symbol)) {
            FunType fun = funStore.getFun(symbol);
            if (fun != null) {
                return fun;
            } else {
                funStore.setFun(symbol, FunType.valueOf(symbol));
                return funStore.getFun(symbol);
            }
        }
        return ValueType.from(symbol);
        //        throw new IllegalArgumentException("Invalid symbol: " + symbol);
    }

    private static boolean isFunction(String symbol) {
        return symbol.startsWith("(");
    }

    public static Type from(String string) {
        var res = ValueType.from(string);
        if (res != null) {
            return res;
        }
        for (ReferenceType value : ReferenceType.values()) {
            if (StringUtils.equals(value.getValue(), string)) {
                return value;
            }
        }
        return new ReferenceType(string);
    }
}
