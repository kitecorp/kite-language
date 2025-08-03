package io.kite.TypeChecker.Types;

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
        return ValueType.of(symbol);
        //        throw new IllegalArgumentException("Invalid symbol: " + symbol);
    }

    private static boolean isFunction(String symbol) {
        return symbol.startsWith("(");
    }

    public static Type of(String string) {
        var res = ValueType.of(string);
        if (res == null) {
            return ReferenceType.of(string);
        }
        return res;
    }
}
