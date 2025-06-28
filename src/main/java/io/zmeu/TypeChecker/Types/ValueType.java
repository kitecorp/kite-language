package io.zmeu.TypeChecker.Types;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class ValueType extends Type {
    public static ValueType String = new ValueType("string");
    public static ValueType Number = new ValueType("number");
    public static ValueType Boolean = new ValueType("boolean");
    public static ValueType Void = new ValueType("void");
    public static ValueType Null = new ValueType("null");

    private ValueType() {
        super();
    }

    private ValueType(String value) {
        super();
        setValue(value);
    }

    public static ValueType of(String value) {
        return switch (value) {
            case "boolean" -> Boolean;
            case "string" -> String;
            case "number" -> Number;
            case "void" -> Void;
            case "null" -> Null;
            default -> null;
        };
    }

    @Override
    public java.lang.String toString() {
        return getValue();
    }


}
